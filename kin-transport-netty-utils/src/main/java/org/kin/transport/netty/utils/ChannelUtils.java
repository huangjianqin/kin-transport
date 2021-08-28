package org.kin.transport.netty.utils;

import io.netty.channel.Channel;

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
}
