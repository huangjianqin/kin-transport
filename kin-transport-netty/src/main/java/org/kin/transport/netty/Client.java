package org.kin.transport.netty;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.EncoderException;
import org.jctools.queues.MpscLinkedQueue;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.ExceptionUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

/**
 * client抽象, 统一payload处理流程, 重连逻辑
 *
 * @author huangjianqin
 * @date 2023/1/20
 */
public abstract class Client<PT extends ProtocolTransport<PT>> implements Disposable, LoggerOprs {
    /** 原子更新session字段 */
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<Client, Session> SESSION_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Client.class, Session.class, "session");
    /** 重连scheduler */
    private static final Scheduler RECONNECT_SCHEDULER = Schedulers.newSingle("kin-client-reconnect", true);

    static {
        JvmCloseCleaner.instance().add(RECONNECT_SCHEDULER::dispose);
    }

    /** client配置 */
    protected final PT clientTransport;
    /** client断开连接期间堆积的协议, 并非保证exactly once, 已经flush的协议可能会丢失 */
    private final MpscLinkedQueue<OutboundPayload> waitingPayloads = new MpscLinkedQueue<>();
    /** 会话 */
    private volatile Session session;
    /** client是否closed */
    private volatile boolean disposed;

    protected Client(PT clientTransport) {
        this.clientTransport = clientTransport;
    }

    /**
     * 获取connect逻辑
     *
     * @return connect逻辑
     */
    protected abstract Mono<Session> connector();

    /**
     * @return remote描述
     */
    protected abstract String remoteDesc();

    /**
     * @return client命名
     */
    protected String clientName() {
        return getClass().getSimpleName();
    }

    /**
     * 连接成功绑定inbound payload逻辑处理
     */
    @SuppressWarnings({"unchecked"})
    protected void onConnected(Session session) {
        PayloadProcessor payloadProcessor = clientTransport.getPayloadProcessor();
        session.getConnection()
                .inbound()
                .receiveObject()
                .flatMap(o -> {
                    //有可能一次多个payload
                    if (o instanceof List) {
                        return Flux.fromIterable(((List<Object>) o));
                    } else {
                        return Flux.just(o);
                    }
                })
                .filter(o -> {
                    //过滤非法payload
                    if (!(o instanceof ByteBufPayload)) {
                        log().warn("unexpected payload type received: {}, channel: {}.", o.getClass(), session.channel());
                        return false;
                    }

                    return true;
                })
                .cast(ByteBufPayload.class)
                .flatMap(bp -> payloadProcessor.process(session, bp))
                .onErrorContinue((throwable, o) -> log().error("{} process payload error, {}\r\n{}", clientName(), o, throwable))
                .subscribe();
    }

    /**
     * 连接成功并更新好{@link #session}后执行操作
     */
    private void afterConnected() {
        //尝试重发断连时堆积payload
        OutboundPayload outboundPayload;
        while (Objects.nonNull((outboundPayload = waitingPayloads.poll()))) {
            writeOrCache(outboundPayload).subscribe();
        }
    }

    /**
     * client重连逻辑
     */
    protected void tryReconnect() {
        tryReconnect(0);
    }

    /**
     * client重连逻辑
     */
    @SuppressWarnings("unchecked")
    protected void tryReconnect(int times) {
        if (disposed) {
            return;
        }

        Session session = SESSION_UPDATER.get(this);
        if (Objects.nonNull(session) && session.isActive()) {
            //session存活
            return;
        }

        //初始间隔为1s, 最大间隔3s
        int nextTimes = times + 1;
        Mono.delay(Duration.ofSeconds(Math.min(3L, times)), RECONNECT_SCHEDULER)
                .flatMap(t -> connector())
                .subscribe(s -> {
                    s.getConnection().onDispose(() -> {
                        if (disposed) {
                            return;
                        }

                        log().info("{} prepare to reconnect to remote '{}'", clientName(), remoteDesc());
                        tryReconnect(nextTimes);
                    });
                    //更新session实例
                    SESSION_UPDATER.set(this, s);

                    afterConnected();
                }, t -> handleErrorOnConnecting(t, nextTimes));
    }

    /**
     * 处理connect过程的异常
     */
    private void handleErrorOnConnecting(Throwable t, int times) {
        log().error("{} connect to remote '{}' error", clientName(), remoteDesc(), t);
        tryReconnect(times);
    }

    /**
     * 获取会话, 首次连接未建立时, 会等待
     *
     * @return 会话
     */
    @SuppressWarnings("unchecked")
    public Mono<Session> session() {
        //用于首次connect时, 创建等待complete signal的{@link Mono}, 后续都会取到一个session实例, 但是已经disposed
        return Mono.justOrEmpty(SESSION_UPDATER.get(this));
    }

    /**
     * 构造{@link OutboundPayload}
     */
    private OutboundPayload newOutboundPayload(Session session, Consumer<OutboundPayload> encoder) {
        OutboundPayload outboundPayload;
        if (Objects.nonNull(session)) {
            outboundPayload = session.newOutboundPayload();
        } else {
            //连接未建立 or 连接断开
            outboundPayload = new OutboundPayload(PooledByteBufAllocator.DEFAULT.directBuffer());
        }
        encoder.accept(outboundPayload);

        return outboundPayload;
    }

    /**
     * client write outbound, 如果失败, 则丢失
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> write(Consumer<OutboundPayload> encoder) {
        return session().flatMap(s -> session.write(newOutboundPayload(s, encoder)));
    }

    /**
     * client write outbound, 如果失败, 则丢失
     *
     * @param encoder  协议对象 -> bytes payload逻辑
     * @param listener netty channel operation callback
     * @return complete signal
     */
    public Mono<Void> write(Consumer<OutboundPayload> encoder, ChannelOperationListener<Session> listener) {
        return session().flatMap(s -> session.write(newOutboundPayload(s, encoder), listener));
    }

    /**
     * client write outbound, 如果失败, 则缓存, 当重连成功时, 重发
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> writeOrCache(Consumer<OutboundPayload> encoder) {
        return session().flatMap(s -> writeOrCache(newOutboundPayload(s, encoder)));
    }

    /**
     * client write outbound, 如果失败, 则缓存, 当重连成功时, 重发
     *
     * @param outboundPayload outbound payload
     * @return complete signal
     */
    private Mono<Void> writeOrCache(OutboundPayload outboundPayload) {
        return session.write(outboundPayload, new ChannelOperationListener<Session>() {
            @Override
            public void onFailure(Session session, Throwable cause) {
                //过滤某些异常
                if (cause instanceof EncoderException) {
                    //payload无法encode, ignore
                    ExceptionUtils.throwExt(cause);
                    return;
                }
                waitingPayloads.add(outboundPayload);
            }
        });
    }

    @Override
    public void dispose() {
        dispose(null);
    }

    public void dispose(@Nullable Disposable disposable) {
        disposed = true;
        Session session = SESSION_UPDATER.get(this);
        if (Objects.nonNull(session)) {
            if (Objects.nonNull(disposable)) {
                //自定义dispose逻辑
                session.getConnection().onDispose(disposable);
            }
            session.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
