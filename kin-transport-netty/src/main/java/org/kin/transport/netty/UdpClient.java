package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class UdpClient extends ClientConnection {
    protected static final Logger log = LoggerFactory.getLogger(Client.class);

    protected EventLoopGroup group;
    protected volatile Channel channel;
    protected volatile boolean isStopped;

    public UdpClient(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void connect(AbstractTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) {
        log.info("client({}) connecting...", address);

        Map<ChannelOption, Object> channelOptions = transportOption.getChannelOptions();

        Preconditions.checkArgument(channelOptions != null);
        Preconditions.checkArgument(channelHandlerInitializer != null);

        group = new NioEventLoopGroup();

        CountDownLatch latch = new CountDownLatch(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class);

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
        ChannelFuture cf = bootstrap.bind(0);
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
        try {
            latch.await();
        } catch (InterruptedException e) {

        }
    }

    @Override
    public void close() {
        if (isStopped) {
            return;
        }
        isStopped = true;
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
        log.info("client({}) closed", address);
    }

    @Override
    public boolean isActive() {
        return !isStopped && channel != null && channel.isActive();
    }

    /**
     * 请求消息
     */
    public void request(byte[] bytes) {
        request(bytes);
    }

    /**
     * 请求消息
     */
    public void request(byte[] bytes, ChannelFutureListener... listeners) {
        if (isActive() && Objects.nonNull(bytes) && bytes.length > 0) {
            ChannelFuture channelFuture = channel.writeAndFlush(new DatagramPacket(bytes, bytes.length, address));
            if (CollectionUtils.isNonEmpty(listeners)) {
                channelFuture.addListeners(listeners);
            }
        }
    }

    public String getLocalAddress() {
        if (channel != null) {
            return channel.localAddress().toString();
        }
        return "";
    }

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
        UdpClient udpClient = (UdpClient) o;
        return Objects.equals(channel, udpClient.channel);
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }
}
