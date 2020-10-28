package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

        CountDownLatch latch = new CountDownLatch(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);

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
        cf.addListener((ChannelFuture channelFuture) -> {
            if (channelFuture.isSuccess()) {
                log.info("connect to remote server success: {}", address);
                channel = channelFuture.channel();
                latch.countDown();
            } else {
                log.error("connect to remote server fail: {}", address);
                latch.countDown();
            }
        });

        long connectTimeout = transportOption.getConnectTimeout();
        try {
            if (connectTimeout > 0) {
                boolean success = latch.await(transportOption.getConnectTimeout(), TimeUnit.MILLISECONDS);
                if (!success) {
                    throw new ClientConnectTimeoutException(address.toString());
                }
            } else {
                latch.await();
            }
        } catch (InterruptedException e) {

        } catch (Exception e) {
            log.error("", e);

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
     * 请求消息
     */
    public boolean request(MSG msg) {
        return request(msg, new ChannelFutureListener[0]);
    }

    /**
     * 请求消息
     */
    public boolean request(MSG msg, ChannelFutureListener... listeners) {
        if (isActive() && Objects.nonNull(msg)) {
            ChannelFuture channelFuture = channel.writeAndFlush(msg);
            if (CollectionUtils.isNonEmpty(listeners)) {
                channelFuture.addListeners(listeners);
            }
            return true;
        }

        return false;
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
