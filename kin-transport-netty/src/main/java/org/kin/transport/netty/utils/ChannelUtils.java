package org.kin.transport.netty.utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.userevent.GlobalRatelimitEvent;

import java.net.InetSocketAddress;

/**
 * netty channel 工具类
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelUtils {
    private ChannelUtils() {

    }

    /**
     * 获取该channel的远程地址
     */
    public static String getRemoteIp(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().toString().substring(1);
    }

    public static <T> void handleUserEvent(Object evt, ChannelHandlerContext ctx, TransportHandler<T> transportHandler) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                transportHandler.readIdle(ctx);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                transportHandler.writeIdel(ctx);
            } else {
                //All IDLE
                transportHandler.readWriteIdle(ctx);
            }
        }
        if (evt instanceof GlobalRatelimitEvent) {
            transportHandler.globalRateLimitReject();
        }
    }
}
