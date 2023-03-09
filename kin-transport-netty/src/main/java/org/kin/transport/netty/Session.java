package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.utils.AdaptiveOutputByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 会话
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class Session implements Disposable {
    private static final Logger log = LoggerFactory.getLogger(Session.class);
    /** session channel key */
    public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");

    /** 协议配置 */
    private final ProtocolOptions options;
    /** session绑定的{@link  Connection}实例 */
    private volatile Connection connection;
    /** 标识是否closed */
    private volatile boolean disposed;
    /** 自适应分配bytebuf */
    private final AdaptiveOutputByteBufAllocator.Handle adaptiveHandle = AdaptiveOutputByteBufAllocator.DEFAULT.newHandle();

    /**
     * 获取{@code connection}绑定的session
     */
    @Nullable
    public static Session current(Connection connection) {
        if (!connection.channel().eventLoop().inEventLoop()) {
            throw new TransportException("it is illegal to get session on non event loop");
        }
        Attribute<Session> sessionAttribute = connection.channel().attr(SESSION_KEY);
        return sessionAttribute.get();
    }

    /**
     * 构建无连接的session, 常用于client端建立connection或建立自动重连的connection
     */
    public Session(ProtocolOptions options) {
        this.options = options;
    }

    /**
     * 基于已建立的connection构建session, 常用于server维护client session
     */
    public Session(ProtocolOptions options, Connection connection) {
        this.options = options;
        bind(connection);
    }

    /**
     * 底层连接建立时调用, 以绑定已连接的connection
     */
    public void bind(Connection connection) {
        Connection oldConnection = this.connection;
        if (Objects.nonNull(oldConnection) && !oldConnection.isDisposed()) {
            //shutdown old connection
            oldConnection.dispose();
        }
        this.connection = connection;

        //bind session to channel
        Attribute<Session> sessionAttribute = channel().attr(SESSION_KEY);
        sessionAttribute.set(this);

        log.info("session bound on channel {}", channel());
        connection.onDispose(() -> {
            log.info("session unbound on channel {}", channel());
        });
    }

    /**
     * 获取{@link Connection}实例绑定的netty channel
     */
    public Channel channel() {
        if (Objects.isNull(connection)) {
            return null;
        }

        return connection.channel();
    }

    /**
     * write out
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     */
    public Mono<Void> write(@Nonnull Consumer<OutboundPayload> encoder) {
        OutboundPayload outboundPayload = newOutboundPayload();
        encoder.accept(outboundPayload);
        return write(outboundPayload);
    }

    /**
     * write out
     */
    public Mono<Void> write(@Nonnull OutboundPayload payload) {
        if (!isActive()) {
            return Mono.error(new TransportException("channel inactive, " + channel()));
        }

        return write0(payload);
    }

    /**
     * write out
     */
    private Mono<Void> write0(@Nonnull OutboundPayload payload) {
        //sendObject相当于channel.writeAndFlush
        return connection.outbound().sendObject(payload).then();
    }

    /**
     * write out
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     */
    public Mono<Void> write(@Nonnull Consumer<OutboundPayload> encoder, @Nonnull ChannelOperationListener<Session> listener) {
        OutboundPayload outboundPayload = newOutboundPayload();
        encoder.accept(outboundPayload);
        return write(outboundPayload, listener);
    }

    /**
     * write out
     */
    public Mono<Void> write(@Nonnull OutboundPayload payload, @Nonnull ChannelOperationListener<Session> listener) {
        Mono<Void> result;
        if (!isActive()) {
            result = Mono.error(new TransportException("channel inactive, " + channel()));
        } else {
            result = write0(payload);
        }

        return result.doOnSuccess(v -> listener.onSuccess(this))
                .doOnError(t -> listener.onFailure(this, t));
    }

    /**
     * write out and close session
     *
     * @param encoder 协议对象 -> bytes payload逻辑
     */
    public Mono<Void> writeAndClose(@Nonnull Consumer<OutboundPayload> encoder) {
        OutboundPayload outboundPayload = newOutboundPayload();
        encoder.accept(outboundPayload);
        return writeAndClose(outboundPayload);
    }

    /**
     * write out and close session
     */
    public Mono<Void> writeAndClose(@Nonnull OutboundPayload payload) {
        if (!isActive()) {
            return Mono.empty();
        }

        channel().eventLoop().schedule(this::dispose, 300, TimeUnit.MILLISECONDS);
        return write(payload, new ChannelOperationListener<Session>() {
            @Override
            public void onSuccess(Session session) {
                dispose();
            }

            @Override
            public void onFailure(Session session, Throwable cause) {
                dispose();
            }
        });
    }

    /**
     * close session
     */
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        this.disposed = true;
        connection.dispose();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * session是否有效
     */
    public boolean isActive() {
        return !disposed && connection != null && channel().isActive();
    }

    /**
     * 获取底层连接{@link Channel#alloc()}, 如果connection未建立, 则返回{@link io.netty.buffer.PooledByteBufAllocator#DEFAULT}
     */
    private ByteBufAllocator getChannelAlloc() {
        if (isActive()) {
            return channel().alloc();
        } else {
            return PooledByteBufAllocator.DEFAULT;
        }
    }

    /**
     * 分配新的自适应大小的outbound bytebuf
     */
    public OutboundPayload newOutboundPayload() {
        ByteBuf byteBuf = adaptiveHandle.allocate(getChannelAlloc())
                .ensureWritable(options.getHeaderSize())
                .writerIndex(options.getHeaderSize());
        return new OutboundPayload(adaptiveHandle, byteBuf);
    }

    /**
     * {@link Channel#id()}
     */
    public String id() {
        // 注意这里的id并不是全局唯一, 单节点中是唯一的
        return channel().id().asShortText();
    }

    /**
     * {@link EventLoop#inEventLoop()}
     */
    public boolean inEventLoop() {
        return channel().eventLoop().inEventLoop();
    }

    /**
     * {@link Channel#localAddress()}
     */
    public SocketAddress localAddress() {
        return channel().localAddress();
    }

    /**
     * {@link Channel#remoteAddress()}
     */
    public SocketAddress remoteAddress() {
        return channel().remoteAddress();
    }

    /**
     * {@link Channel#isWritable()}
     */
    public boolean isWritable() {
        return channel().isWritable();
    }

    /**
     * {@link ChannelConfig#isAutoRead()}
     */
    public boolean isAutoRead() {
        return channel().config().isAutoRead();
    }

    /**
     * {@link ChannelConfig#setAutoRead(boolean)}
     */
    public void setAutoRead(boolean autoRead) {
        channel().config().setAutoRead(autoRead);
    }

    //getter
    public Connection connection() {
        return connection;
    }
}
