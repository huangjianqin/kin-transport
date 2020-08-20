package org.kin.transport.netty.core;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.kin.framework.utils.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * server
 * 阻塞绑定端口
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public class Server extends ServerConnection {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    /** selector线程池 */
    private NioEventLoopGroup bossGroup;
    /** worker线程池 */
    private NioEventLoopGroup workerGroup;
    /** selector */
    private volatile Channel selector;

    public Server(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void bind(ServerTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) throws Exception {
        log.info("server({}) connection binding...", address);

        Map<ChannelOption, Object> serverOptions = transportOption.getServerOptions();
        Map<ChannelOption, Object> channelOptions = transportOption.getChannelOptions();

        //校验
        Preconditions.checkArgument(bossGroup == null);
        Preconditions.checkArgument(workerGroup == null);
        Preconditions.checkArgument(serverOptions != null);
        Preconditions.checkArgument(channelOptions != null);
        Preconditions.checkArgument(channelHandlerInitializer != null);

        //一半的CPU用于selector
        this.bossGroup = new NioEventLoopGroup(SysUtils.CPU_NUM / 2 + 1);
        //默认2倍cpu
        this.workerGroup = new NioEventLoopGroup();

        CountDownLatch latch = new CountDownLatch(1);

        //配置bootstrap
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class);

        for (Map.Entry<ChannelOption, Object> entry : serverOptions.entrySet()) {
            bootstrap.option(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<ChannelOption, Object> entry : channelOptions.entrySet()) {
            bootstrap.childOption(entry.getKey(), entry.getValue());
        }

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                for (ChannelHandler channelHandler : channelHandlerInitializer.getChannelHandlers()) {
                    socketChannel.pipeline().addLast(channelHandler.getClass().getSimpleName(), channelHandler);
                }
            }
        });

        //绑定
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

        //help gc
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

        return Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return selector != null ? selector.hashCode() : 0;
    }
}
