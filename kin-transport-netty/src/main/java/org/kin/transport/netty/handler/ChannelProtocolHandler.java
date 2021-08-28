package org.kin.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.socket.ProtocolRateLimiter;
import org.kin.transport.netty.userevent.GlobalRatelimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 协议层逻辑处理
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelProtocolHandler<MSG> extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelProtocolHandler.class);
    private final ProtocolHandler<MSG> protocolHandler;

    public ChannelProtocolHandler(ProtocolHandler<MSG> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //合并解包
        List<MSG> protocols = new ArrayList<>();
        if (msg instanceof List) {
            protocols.addAll((Collection<? extends MSG>) msg);
        }
        protocols.add((MSG) msg);

        for (MSG protocol : protocols) {
            log.debug("Recv {} {}", protocol, ctx.channel().remoteAddress());

            //流控
            if (ProtocolRateLimiter.valid(protocol)) {
                protocolHandler.handle(ctx, protocol);
            } else {
                protocolHandler.rateLimitReject(ctx, protocol);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel active: {}", channel.remoteAddress());
        protocolHandler.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel inactive: {}", channel.remoteAddress());
        protocolHandler.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("encounter exception:", cause);
        protocolHandler.handleException(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
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
}
