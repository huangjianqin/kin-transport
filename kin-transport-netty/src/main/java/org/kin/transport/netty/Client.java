package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.log.LoggerOprs;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.Connection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * client抽象, 统一payload处理流程, 重连逻辑
 *
 * @author huangjianqin
 * @date 2023/1/20
 */
public abstract class Client<PT extends AbstractProtocolTransport<PT>> implements Disposable, LoggerOprs {
    /** 原子更新{@link #session}字段 */
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
    /**
     * 会话sink, 用于连接还没建立且writeXX调用时, 注册subscriber, 等连接建立成功后, 发送请求
     * 首次连接成功后, emit {@link Session}实例, 有且仅有一次, 因为重来不会创建多个新的{@link Session}实例
     * 而是仅仅替换{@link Session}的{@link Connection}实例
     */
    private final Sinks.One<Session> sessionSink = Sinks.one();
    /**
     * 会话
     * 首次连接成功后会创建新的{@link Session}实例
     * 断连重连后, 会调用{@link Session#bind(Connection)}替换底层{@link Connection}实例
     */
    private volatile Session session;
    /** client是否closed */
    private volatile boolean disposed;

    protected Client(PT clientTransport) {
        this.clientTransport = clientTransport;
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
                        log().warn("unexpected payload type received: {}, channel: {}.", o.getClass(), SESSION_UPDATER.get(this).channel());
                        return false;
                    }

                    return true;
                })
                .cast(ByteBufPayload.class)
                .flatMap(bp -> payloadProcessor.process(SESSION_UPDATER.get(this), bp))
                .onErrorContinue((throwable, o) -> log().error("{} process payload error, {}\r\n{}", clientName(), o, throwable))
                .subscribe();

        //自定义connection断开逻辑
        connection.onDispose(() -> {
            if (disposed) {
                return;
            }

            inboundProcessDisposable.dispose();
            //尝试重连
            log().info("{} prepare to reconnect to remote '{}'", clientName(), remoteAddress());
            tryReconnect(retryTimes);
        });

        Session session = SESSION_UPDATER.get(this);
        if (Objects.isNull(session)) {
            //new session
            session = new Session(clientTransport.getProtocolOptions(), connection);
            sessionSink.emitValue(session, RetryNonSerializedEmitFailureHandler.RETRY_NON_SERIALIZED);
            SESSION_UPDATER.set(this, session);
        } else {
            //替换session里面的connection实例
            session.bind(connection);
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

        Session session = SESSION_UPDATER.get(this);
        if (session != null && session.isActive()) {
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
        return sessionSink.asMono();
    }

    /**
     * client send payload, 如果失败, 则丢失
     *
     * @param encoder 数据对象 -> bytes payload逻辑
     * @return complete signal
     */
    public <T> Mono<Void> sendObject(@Nonnull T obj, @Nonnull ObjectEncoder<T> encoder) {
        return session().flatMap(s -> s.sendObject(obj, encoder));
    }

    /**
     * client send payload, 如果失败, 则丢失
     *
     * @param encoder  数据对象 -> bytes payload逻辑
     * @param listener netty channel operation callback
     * @return complete signal
     */
    public <T> Mono<Void> sendObject(@Nonnull T obj, @Nonnull ObjectEncoder<T> encoder,
                                     @Nonnull ChannelOperationListener listener) {
        return session().flatMap(s -> s.sendObject(obj, encoder, listener));
    }

    /**
     * client send bytebuf, 如果失败, 则丢失
     *
     * @return complete signal
     */
    public Mono<Void> send(@Nonnull ByteBuf byteBuf) {
        return session().flatMap(s -> s.send(byteBuf));
    }

    /**
     * client send bytebuf, 如果失败, 则丢失
     * <p>
     * \     * @param listener netty channel operation callback
     *
     * @return complete signal
     */
    public Mono<Void> send(@Nonnull ByteBuf byteBuf, @Nonnull ChannelOperationListener listener) {
        return session().flatMap(s -> s.send(byteBuf, listener));
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
                session.connection().onDispose(disposable);
            }
            session.dispose();
        } else {
            //连接还没建立就dispose, 直接complete sink
            sessionSink.emitEmpty(RetryNonSerializedEmitFailureHandler.RETRY_NON_SERIALIZED);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
