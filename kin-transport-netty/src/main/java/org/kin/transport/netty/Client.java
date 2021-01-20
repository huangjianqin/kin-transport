package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * client
 * (超时)阻塞连接远程服务器
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public class Client<MSG> extends ClientConnection {
    protected static final Logger log = LoggerFactory.getLogger(Client.class);

    protected volatile EventLoopGroup group;
    protected volatile Channel channel;
    protected volatile boolean isStopped;
    private AtomicBoolean flushScheduleFlag = new AtomicBoolean(false);

    public Client(AbstractTransportOption<?, ?, ?, ?> transportOption, ChannelHandlerInitializer<?, ?, ?> channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    @Override
    public void connect(InetSocketAddress address) {
        if (isStopped()) {
            return;
        }
        if (isActive()) {
            return;
        }
        log.info("client({}) connecting...", address);

        Map<ChannelOption, Object> channelOptions = transportOption.getChannelOptions();

        Preconditions.checkArgument(channelOptions != null);
        Preconditions.checkArgument(channelHandlerInitializer != null);

        group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NettyUtils.getChannelClass());

        for (Map.Entry<ChannelOption, Object> entry : channelOptions.entrySet()) {
            bootstrap.option(entry.getKey(), entry.getValue());
        }

        final SslContext sslCtx;
        if (transportOption.isSsl()) {
            try {
                sslCtx = SslContextBuilder.forClient().keyManager(transportOption.getCertFile(), transportOption.getCertKeyFile()).build();
            } catch (SSLException e) {
                throw new IllegalStateException(e);
            }
        } else {
            sslCtx = null;
        }

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                if (Objects.nonNull(sslCtx)) {
                    pipeline.addLast(sslCtx.newHandler(socketChannel.alloc(), address.getHostString(), address.getPort()));
                }

                for (ChannelHandler channelHandler : channelHandlerInitializer.getChannelHandlers()) {
                    pipeline.addLast(channelHandler.getClass().getSimpleName(), channelHandler);
                }
            }
        });
        ChannelFuture cf = bootstrap.connect(address);

        long awaitTimeout = transportOption.getAwaitTimeout();
        try {
            if (awaitTimeout > 0) {
                boolean success = cf.await(awaitTimeout, TimeUnit.MILLISECONDS);
                if (!success) {
                    throw new ClientConnectTimeoutException(address.toString());
                }
            } else {
                cf.await();
            }
        } catch (Exception e) {
            log.error("", e);
        }

        if (cf.isSuccess()) {
            log.info("connect to remote server success: {}", address);
            channel = cf.channel();
        } else {
            log.error("connect to remote server fail: {}", address);
        }
    }

    @Override
    public void close() {
        if (isStopped()) {
            return;
        }
        String addressStr = getAddress();
        isStopped = true;
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
        group = null;
        channel = null;
        log.info("client({}) closed", addressStr);
    }

    @Override
    public boolean isActive() {
        return !isStopped() && channel != null && channel.isActive();
    }

    /**
     * @return connect address
     */
    @Override
    public String getAddress() {
        if (Objects.isNull(channel)) {
            return "unknown";
        }

        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        if (Objects.isNull(address)) {
            return "unknown";
        }
        return address.getHostName() + ":" + address.getPort();
    }

    /**
     * 发送消息
     */
    public boolean sendAndFlush(MSG msg) {
        return sendAndFlush(msg, new ChannelFutureListener[0]);
    }

    /**
     * 发送消息
     */
    public boolean sendAndFlush(MSG msg, ChannelFutureListener... listeners) {
        if (!isActive() || !Objects.nonNull(msg)) {
            return false;
        }

        ChannelFuture channelFuture = channel.writeAndFlush(msg);
        if (CollectionUtils.isNonEmpty(listeners)) {
            channelFuture.addListeners(listeners);
        }
        return true;
    }

    /**
     * 发送消息, 仅仅将消息推进socket buff
     */
    public boolean sendWithoutFlush(MSG msg) {
        return sendWithoutFlush(msg, new ChannelFutureListener[0]);
    }

    /**
     * 发送消息, 仅仅将消息推进socket buff
     */
    public boolean sendWithoutFlush(MSG msg, ChannelFutureListener... listeners) {
        return sendAndScheduleFlush(msg, 0, null, listeners);
    }

    /**
     * 发送消息, 将消息推进socket buff, 并调度flush
     */
    public boolean sendAndScheduleFlush(MSG msg, int time, TimeUnit timeUnit) {
        return sendAndScheduleFlush(msg, time, timeUnit, new ChannelFutureListener[0]);
    }

    /**
     * 发送消息, 将消息推进socket buff, 并调度flush
     */
    public boolean sendAndScheduleFlush(MSG msg, int time, TimeUnit timeUnit, ChannelFutureListener... listeners) {
        if (!isActive() || !Objects.nonNull(msg)) {
            return false;
        }

        ChannelFuture channelFuture = channel.write(msg);
        if (CollectionUtils.isNonEmpty(listeners)) {
            channelFuture.addListeners(listeners);
        }
        if (time > 0) {
            //schedule flush
            if (flushScheduleFlag.compareAndSet(false, true)) {
                channel.eventLoop().schedule(() -> {
                    if (flushScheduleFlag.compareAndSet(true, false)) {
                        channel.flush();
                    }
                }, time, timeUnit);
            }
        }
        return true;
    }

    /**
     * @return channel local address
     */
    public String getLocalAddress() {
        if (channel != null) {
            return channel.localAddress().toString();
        }
        return null;
    }

    /**
     * @return client是否stopped
     */
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Client that = (Client) o;

        return Objects.equals(channel, that.channel);
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }
}
