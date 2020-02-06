package org.kin.transport.netty.core;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by huangjianqin on 2019/5/30.
 */
public class Server extends AbstractConnection {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    //连接相关线程池
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private volatile Channel selector;

    public Server(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void connect(Map<ChannelOption, Object> channelOptions, ChannelHandlerInitializer channelHandlerInitializer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(Map<ChannelOption, Object> channelOptions, ChannelHandlerInitializer channelHandlerInitializer) throws Exception {
        log.info("server({}) connection binding...", address);

        Preconditions.checkArgument(bossGroup == null);
        Preconditions.checkArgument(workerGroup == null);
        Preconditions.checkArgument(channelOptions != null);
        Preconditions.checkArgument(channelHandlerInitializer != null);

        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        CountDownLatch latch = new CountDownLatch(1);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class);

        for (Map.Entry<ChannelOption, Object> entry : channelOptions.entrySet()) {
            bootstrap.option(entry.getKey(), entry.getValue());
        }

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(channelHandlerInitializer.getChannelHandlers());
            }
        });
        ChannelFuture cf = bootstrap.bind(super.address);
        cf.addListener((ChannelFuture channelFuture) -> {
            if (channelFuture.isSuccess()) {
                log.info("server connection binded: {}", address);
                selector = channelFuture.channel();
            }
            latch.countDown();
        });

        latch.await();
        if (selector == null) {
            throw new RuntimeException("server connection bind fail: " + address);
        }
    }

    @Override
    public void close() {
        if (this.selector == null || this.workerGroup == null || this.bossGroup == null) {
            return;
        }

        this.selector.close();
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();

        this.selector = null;
        this.workerGroup = null;
        this.bossGroup = null;

        log.info("server connection closed");
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

        Server that = (Server) o;

        return selector != null ? selector.equals(that.selector) : that.selector == null;
    }

    @Override
    public int hashCode() {
        return selector != null ? selector.hashCode() : 0;
    }
}
