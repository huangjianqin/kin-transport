package org.kin.transport.netty.utils;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.userevent.GlobalRatelimitEvent;

import java.net.InetSocketAddress;
import java.util.Objects;

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

    /**
     * 处理netty channel user event
     */
    public static <T> void handleUserEvent(Object evt, ChannelHandlerContext ctx, ProtocolHandler<T> protocolHandler) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                protocolHandler.readIdle(ctx);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                protocolHandler.writeIdle(ctx);
            } else {
                //All IDLE
                protocolHandler.readWriteIdle(ctx);
            }
        }
        if (evt instanceof GlobalRatelimitEvent) {
            protocolHandler.globalRateLimitReject();
        }
    }

    /**
     * 判断是否全局流控限制
     */
    public static boolean globalRateLimit(ChannelHandlerContext ctx, RateLimiter globalRateLimiter) {
        if (Objects.nonNull(globalRateLimiter) && !globalRateLimiter.tryAcquire()) {
            //全局流控
            ctx.fireUserEventTriggered(new GlobalRatelimitEvent());
            return true;
        }

        return false;
    }
}
