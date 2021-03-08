package org.kin.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author huangjianqin
 * @date 2020/12/12
 */
public class NettyUtils {
    private NettyUtils() {
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static Class<? extends ServerChannel> getServerChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static Class<? extends Channel> getChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static EventLoopGroup getEventLoopGroup(int nThreads) {
        nThreads = Math.max(nThreads, 0);
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(nThreads);
        }
        return new NioEventLoopGroup(nThreads);
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static EventLoopGroup getEventLoopGroup() {
        return getEventLoopGroup(0);
    }
}
