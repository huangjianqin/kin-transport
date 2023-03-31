package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.utils.ByteBufUtils;
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
        return current(connection.channel());
    }

    /**
     * 获取{@code connection}绑定的session
     */
    @Nullable
    public static Session current(Channel channel) {
        if (!channel.eventLoop().inEventLoop()) {
            throw new TransportException("it is illegal to get session on non event loop");
        }
        Attribute<Session> sessionAttribute = channel.attr(SESSION_KEY);
        return sessionAttribute.get();
    }

    /**
     * 基于已建立的connection构建session实例
     */
    public Session(ProtocolOptions options, Connection connection) {
        this.options = options;
        bind(connection);
    }

    /**
     * 底层连接建立时调用, 以绑定新connection
     * 往往用于旧连接断开, 新连接建立成功后, 替换session实例的底层connection
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
        return connection.channel();
    }

    /**
     * send object
     *
     * @param encoder 数据对象 -> bytes payload逻辑
     */
    public <T> Mono<Void> sendObject(@Nonnull T obj, @Nonnull ObjectEncoder<T> encoder) {
        ByteBufPayload outboundPayload = newOutboundPayload();
        encoder.encode(obj, outboundPayload);
        return send0(outboundPayload);
    }

    /**
     * send bytebuf
     *
     * @param data data
     */
    public Mono<Void> send(@Nonnull ByteBuf data) {
        return send0(newOutboundPayload(data));
    }

    /**
     * send
     *
     * @param payload 保证底层bytebuf拥有(header(预占)+传输内容)bytes
     */
    private Mono<Void> send0(@Nonnull ByteBufPayload payload) {
        if (!isActive()) {
            return Mono.error(new TransportException("channel inactive, " + channel()));
        }

        return send1(payload);
    }

    /**
     * send
     */
    private Mono<Void> send1(@Nonnull ByteBufPayload payload) {
        payload.touch(this);
        //sendObject相当于channel.writeAndFlush
        return connection.outbound().sendObject(payload).then();
    }

    /**
     * send object
     *
     * @param encoder 数据对象 -> bytes payload逻辑
     */
    public <T> Mono<Void> sendObject(@Nonnull T obj, @Nonnull ObjectEncoder<T> encoder,
                                     @Nonnull ChannelOperationListener listener) {
        ByteBufPayload outboundPayload = newOutboundPayload();
        encoder.encode(obj, outboundPayload);
        return send0(outboundPayload, listener);
    }

    /**
     * send bytebuf
     *
     * @param data data
     */
    public Mono<Void> send(@Nonnull ByteBuf data, @Nonnull ChannelOperationListener listener) {
        return send0(newOutboundPayload(data), listener);
    }

    /**
     * send
     *
     * @param payload 保证底层bytebuf拥有(header(预占)+传输内容)bytes
     */
    private Mono<Void> send0(@Nonnull ByteBufPayload payload, @Nonnull ChannelOperationListener listener) {
        Mono<Void> result;
        if (!isActive()) {
            result = Mono.error(new TransportException("channel inactive, " + channel()));
        } else {
            result = send1(payload);
        }

        return result.doOnSuccess(v -> listener.onSuccess(this))
                .doOnError(t -> listener.onFailure(this, t));
    }

    /**
     * send and close session
     *
     * @param encoder 数据对象 -> bytes payload逻辑
     */
    public <T> Mono<Void> sendObjectAndClose(@Nonnull T obj, @Nonnull ObjectEncoder<T> encoder) {
        ByteBufPayload outboundPayload = newOutboundPayload();
        encoder.encode(obj, outboundPayload);
        return sendAndClose0(outboundPayload);
    }

    /**
     * send and close session
     *
     * @param data data
     */
    public Mono<Void> sendAndClose(@Nonnull ByteBuf data) {
        return sendAndClose0(newOutboundPayload(data));
    }

    /**
     * send and close session
     *
     * @param payload 保证底层bytebuf拥有(header(预占)+传输内容)bytes
     */
    private Mono<Void> sendAndClose0(@Nonnull ByteBufPayload payload) {
        if (!isActive()) {
            return Mono.empty();
        }

        channel().eventLoop().schedule(this::dispose, 300, TimeUnit.MILLISECONDS);
        return send0(payload, new ChannelOperationListener() {
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
    public ByteBufAllocator alloc() {
        if (isActive()) {
            return channel().alloc();
        } else {
            return PooledByteBufAllocator.DEFAULT;
        }
    }

    /**
     * 分配新的自适应大小的outbound bytebuf
     */
    private ByteBufPayload newOutboundPayload() {
        ByteBuf byteBuf = adaptiveHandle.allocate(alloc())
                .ensureWritable(options.getHeaderSize())
                .writerIndex(options.getHeaderSize());
        return ByteBufPayload.create(byteBuf.retain(), adaptiveHandle);
    }

    /**
     * 分配新的自适应大小的outbound bytebuf
     *
     * @param data data
     */
    private ByteBufPayload newOutboundPayload(@Nonnull ByteBuf data) {
        return ByteBufPayload.create(ByteBufUtils.rightShift(data, options.getHeaderSize()).retain(), adaptiveHandle);
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
