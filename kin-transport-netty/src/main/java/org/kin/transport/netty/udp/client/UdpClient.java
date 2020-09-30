package org.kin.transport.netty.udp.client;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.utils.CollectionUtils;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.ClientConnectTimeoutException;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
import org.kin.transport.netty.udp.UdpProtocolDetails;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * udp client
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class UdpClient extends Client<SocketProtocol> {
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

        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel datagramChannel) {
                ChannelPipeline pipeline = datagramChannel.pipeline();
                if (Objects.nonNull(sslCtx)) {
                    pipeline.addLast(sslCtx.newHandler(datagramChannel.alloc(), address.getHostString(), address.getPort()));
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
            boolean success = latch.await(transportOption.getConnectTimeout(), TimeUnit.MILLISECONDS);
            if (!success) {
                throw new ClientConnectTimeoutException(address.toString());
            }
        } catch (InterruptedException e) {

        }
    }

    /**
     * 请求消息
     */
    @Override
    public void request(SocketProtocol protocol, ChannelFutureListener... listeners) {
        if (isActive() && Objects.nonNull(protocol)) {
            ChannelFuture channelFuture =
                    channel.writeAndFlush(UdpProtocolDetails.senderWrapper(protocol, address));
            if (CollectionUtils.isNonEmpty(listeners)) {
                channelFuture.addListeners(listeners);
            }
        }
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
