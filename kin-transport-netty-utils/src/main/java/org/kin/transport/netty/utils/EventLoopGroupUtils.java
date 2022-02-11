package org.kin.transport.netty.utils;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author huangjianqin
 * @date 2022/2/10
 */
public final class EventLoopGroupUtils {
    private EventLoopGroupUtils() {
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static EventLoopGroup getAdaptiveEventLoopGroup(int nThreads) {
        nThreads = Math.max(nThreads, 0);
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(nThreads);
        }
        return new NioEventLoopGroup(nThreads);
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static EventLoopGroup getAdaptiveEventLoopGroup() {
        return getAdaptiveEventLoopGroup(0);
    }
}
