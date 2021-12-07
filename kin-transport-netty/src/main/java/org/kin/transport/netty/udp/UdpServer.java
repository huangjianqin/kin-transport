package org.kin.transport.netty.udp;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.ServerBindTimeoutException;
import org.kin.transport.netty.ServerConnection;
import org.kin.transport.netty.estimator.MessageSizeEstimatorImpl;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * udp server
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class UdpServer extends ServerConnection implements LoggerOprs {
    /** worker线程池 */
    private NioEventLoopGroup workerGroup;
    /** selector */
    private volatile Channel selector;

    public UdpServer(AbstractTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bind(InetSocketAddress address) {
        log().info("server({}) connection binding...", address);

        Map<ChannelOption, Object> selectorOptions = transportOption.getSelectorOptions();
        Map<ChannelOption, Object> channelOptions = transportOption.getChannelOptions();

        //校验
        Preconditions.checkArgument(selectorOptions != null);
        Preconditions.checkArgument(channelOptions != null);
        Preconditions.checkArgument(channelHandlerInitializer != null);

        //默认2倍cpu
        this.workerGroup = new NioEventLoopGroup();

        CountDownLatch latch = new CountDownLatch(1);

        //配置bootstrap
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.workerGroup).channel(NioDatagramChannel.class);

        bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, MessageSizeEstimatorImpl.INSTANCE);
        for (Map.Entry<ChannelOption, Object> entry : selectorOptions.entrySet()) {
            bootstrap.option(entry.getKey(), entry.getValue());
        }

        final SslContext sslCtx;
        if (transportOption.isSsl()) {
            try {
                sslCtx = SslContextBuilder.forServer(transportOption.getCertFile(), transportOption.getCertKeyFile()).build();
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
                    pipeline.addLast(sslCtx.newHandler(datagramChannel.alloc()));
                }

                for (ChannelHandler channelHandler : channelHandlerInitializer.getChannelHandlers()) {
                    pipeline.addLast(channelHandler.getClass().getSimpleName(), channelHandler);
                }
            }
        });

        //绑定
        ChannelFuture cf = bootstrap.bind(address);
        cf.addListener((ChannelFuture channelFuture) -> {
            if (channelFuture.isSuccess()) {
                log().info("server connection binded: {}", address);
                selector = channelFuture.channel();
            }
            latch.countDown();
        });

        long awaitTimeout = transportOption.getAwaitTimeout();
        try {
            if (awaitTimeout > 0) {
                boolean success = latch.await(awaitTimeout, TimeUnit.MILLISECONDS);
                if (!success) {
                    throw new ServerBindTimeoutException(address.toString());
                }
            } else {
                latch.await();
            }
        } catch (InterruptedException e) {

        }
        if (selector == null) {
            throw new IllegalStateException("server connection bind fail: " + address);
        }
    }

    @Override
    public void close() {
        if (this.selector == null || this.workerGroup == null) {
            return;
        }

        this.selector.close();
        this.workerGroup.shutdownGracefully();

        //help gc
        this.selector = null;
        this.workerGroup = null;

        log().info("server connection closed");
    }

    @Override
    public String getAddress() {
        InetSocketAddress address = (InetSocketAddress) selector.localAddress();
        return address.getHostName() + ":" + address.getPort();
    }

    @Override
    public boolean isActive() {
        return selector.isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UdpServer udpServer = (UdpServer) o;
        return Objects.equals(selector, udpServer.selector);
    }

    @Override
    public int hashCode() {
        return selector != null ? selector.hashCode() : 0;
    }
}
