package org.kin.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.kin.framework.utils.SysUtils;

/**
 * @author huangjianqin
 * @date 2020/12/12
 */
public class NettyUtils {
    private NettyUtils() {
    }

    /**
     * 自动识别是否linux, 则使用epoll, 否则是nio
     */
    public static Class<? extends ServerChannel> getServerChannelClass() {
        if (SysUtils.isLinux()) {
            return EpollServerSocketChannel.class;
        }
        return NioServerSocketChannel.class;
    }

    /**
     * 自动识别是否linux, 则使用epoll, 否则是nio
     */
    public static Class<? extends Channel> getChannelClass() {
        if (SysUtils.isLinux()) {
            return EpollSocketChannel.class;
        }
        return NioSocketChannel.class;
    }
}
