package org.kin.transport.netty.common;

import io.netty.handler.codec.EncoderException;
import org.jctools.queues.MpscLinkedQueue;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.log.LoggerOprs;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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
    /** 原子更新connectSignal字段 */
    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<Client, CompletableFuture> CONNECT_SIGNAL_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(Client.class, CompletableFuture.class, "connectSignal");
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
     * 连接成功signal
     * {@link CompletableFuture}实例, 用于首次connect时, 创建等待complete signal的{@link Mono}
     */
    private volatile CompletableFuture<Session> connectSignal = new CompletableFuture<>();

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

        //尝试重发断连时堆积payload
        OutboundPayload outboundPayload;
        while (Objects.nonNull((outboundPayload = waitingPayloads.poll()))) {
            write(session, outboundPayload, true).subscribe();
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
                        CONNECT_SIGNAL_UPDATER.set(this, new CompletableFuture<>());
                        tryReconnect(nextTimes);
                    });
                    //更新session实例
                    SESSION_UPDATER.set(this, s);
                    //连接成功时signal
                    CONNECT_SIGNAL_UPDATER.get(this).complete(s);
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
        return Mono.justOrEmpty(SESSION_UPDATER.get(this)).switchIfEmpty(Mono.fromFuture(CONNECT_SIGNAL_UPDATER.get(this)));
    }

    /**
     * client write outbound, 如果失败, 则丢失
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> write(Consumer<OutboundPayload> encoder) {
        return write(encoder, false);
    }

    /**
     * client write outbound, 如果失败, 则缓存, 当重连成功时, 重发
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> writeOrCache(Consumer<OutboundPayload> encoder) {
        return write(encoder, true);
    }

    /**
     * client write outbound统一逻辑
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @param cache   是否缓存write out失败消息
     * @return complete signal
     */
    private Mono<Void> write(Consumer<OutboundPayload> encoder, boolean cache) {
        return session().flatMap(s -> {
            OutboundPayload outboundPayload = s.newOutboundPayload();
            encoder.accept(outboundPayload);
            return write(s, outboundPayload, cache);
        });
    }

    /**
     * client write outbound统一逻辑
     *
     * @param session 会话
     * @param payload 协议payload
     * @param cache   是否缓存write out失败消息
     * @return complete signal
     */
    private Mono<Void> write(Session session, OutboundPayload payload, boolean cache) {
        if (cache) {
            return session.write(payload, new NettyOperationListener<Session>() {
                @Override
                public void onFailure(Session session, Throwable cause) {
                    //过滤某些异常
                    if (cause instanceof EncoderException) {
                        //payload无法encode, ignore
                        return;
                    }
                    waitingPayloads.add(payload);
                }
            });
        } else {
            return session.write(payload);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        Session session = SESSION_UPDATER.get(this);
        if (Objects.nonNull(session)) {
            session.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
