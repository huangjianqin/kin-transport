package org.kin.transport.netty.utils;

import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * netty channel 工具类
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public final class ChannelUtils {
    private ChannelUtils() {

    }

    /**
     * 获取该channel的远程地址
     */
    public static String getRemoteIp(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().toString().substring(1);
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static Class<? extends ServerChannel> getAdaptiveServerChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    /**
     * 自动识别是否支持, 则使用epoll, 否则是nio
     */
    public static Class<? extends Channel> getAdaptiveChannelClass() {
        if (Epoll.isAvailable()) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }
}
