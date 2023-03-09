package org.kin.transport.netty;

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
import reactor.netty.Connection;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * client抽象, 统一payload处理流程, 重连逻辑
 *
 * @author huangjianqin
 * @date 2023/1/20
 */
public abstract class Client<PT extends ProtocolTransport<PT>> implements Disposable, LoggerOprs {
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
    private final Session session;
    /** client是否closed */
    private volatile boolean disposed;

    protected Client(PT clientTransport) {
        this.clientTransport = clientTransport;
        this.session = new Session(clientTransport.getProtocolOptions());
    }

    /**
     * 返回构建connection逻辑
     *
     * @return 构建connection逻辑
     */
    protected abstract Mono<Connection> connector();

    /**
     * @return remote描述
     */
    protected abstract String remoteAddress();

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
    protected void onConnected(Connection connection, int retryTimes) {
        PayloadProcessor payloadProcessor = clientTransport.getPayloadProcessor();
        Disposable inboundProcessDisposable = connection
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

        //自定义connection断开逻辑
        connection.onDispose(() -> {
            inboundProcessDisposable.dispose();
            //尝试重连
            log().info("{} prepare to reconnect to remote '{}'", clientName(), remoteAddress());
            tryReconnect(retryTimes);
        });

        //替换session里面的connection实例
        session.bind(connection);

        //尝试重发断连时堆积payload
        OutboundPayload outboundPayload;
        while (Objects.nonNull((outboundPayload = waitingPayloads.poll()))) {
            writeOrCache(session, outboundPayload).subscribe();
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
    protected void tryReconnect(int retryTimes) {
        if (disposed) {
            return;
        }

        if (session.isActive()) {
            //session存活
            return;
        }

        //初始间隔为0s, 最大间隔3s
        int nextRetryTimes = retryTimes + 1;
        Mono.delay(Duration.ofSeconds(Math.min(3L, retryTimes)), RECONNECT_SCHEDULER)
                .flatMap(t -> connector())
                .subscribe(connection -> onConnected(connection, nextRetryTimes),
                        t -> handleErrorOnConnecting(t, nextRetryTimes));
    }

    /**
     * 处理connect过程的异常
     */
    private void handleErrorOnConnecting(Throwable t, int times) {
        log().error("{} connect to remote '{}' error", clientName(), remoteAddress(), t);
        tryReconnect(times);
    }

    /**
     * 获取会话, 首次连接未建立时, 会等待
     *
     * @return 会话
     */
    public Mono<Session> session() {
        return Mono.just(session);
    }

    /**
     * client write outbound, 如果失败, 则丢失
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> write(Consumer<OutboundPayload> encoder) {
        return session().flatMap(s -> s.write(encoder));
    }

    /**
     * client write outbound, 如果失败, 则丢失
     *
     * @param encoder  协议对象 -> bytes payload逻辑
     * @param listener netty channel operation callback
     * @return complete signal
     */
    public Mono<Void> write(Consumer<OutboundPayload> encoder, ChannelOperationListener<Session> listener) {
        return session().flatMap(s -> s.write(encoder, listener));
    }

    /**
     * client write outbound, 如果失败, 则缓存, 当重连成功时, 重发
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     * @return complete signal
     */
    public Mono<Void> writeOrCache(Consumer<OutboundPayload> encoder) {
        return session().flatMap(s -> {
            OutboundPayload outboundPayload = s.newOutboundPayload();
            encoder.accept(outboundPayload);
            return writeOrCache(s, outboundPayload);
        });
    }

    /**
     * client write outbound, 如果失败, 则缓存, 当重连成功时, 重发
     *
     * @param outboundPayload outbound payload
     * @return complete signal
     */
    private Mono<Void> writeOrCache(Session session, OutboundPayload outboundPayload) {
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
        if (Objects.nonNull(disposable)) {
            //自定义dispose逻辑
            session.connection().onDispose(disposable);
        }
        session.dispose();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
