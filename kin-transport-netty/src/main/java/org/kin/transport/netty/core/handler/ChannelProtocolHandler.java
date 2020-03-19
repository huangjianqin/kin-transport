package org.kin.transport.netty.core.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.core.TransportHandler;
import org.kin.transport.netty.core.domain.GlobalRatelimitEvent;
import org.kin.transport.netty.core.domain.ProtocolRateLimiter;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelProtocolHandler.class);
    private final TransportHandler transportHandler;

    public ChannelProtocolHandler(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //合并解包
        List<AbstractProtocol> protocols = new ArrayList<>();
        if (msg instanceof List) {
            protocols.addAll((Collection<? extends AbstractProtocol>) msg);
        }
        if (msg instanceof AbstractProtocol) {
            protocols.add((AbstractProtocol) msg);
        }

        for (AbstractProtocol protocol : protocols) {
            log.debug("Recv {} {} {}", protocol.getProtocolId(), protocol.getClass().getSimpleName(), ChannelUtils.getIP(ctx.channel()));

            //流控
            if (ProtocolRateLimiter.valid(protocol)) {
                transportHandler.handleProtocol(ctx.channel(), protocol);
            } else {
                transportHandler.rateLimitReject(ctx.channel(), protocol);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel active: {}", ChannelUtils.getIP(channel));
        transportHandler.channelActive(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel inactive: {}", ChannelUtils.getIP(channel));
        transportHandler.channelInactive(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        log.error("server('{}') throw exception:{}", ChannelUtils.getIP(channel), cause);
        transportHandler.handleException(channel, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                transportHandler.readIdle(ctx.channel());
            } else if (event.state() == IdleState.WRITER_IDLE) {
                transportHandler.writeIdel(ctx.channel());
            } else {
                //All IDLE
                transportHandler.readWriteIdle(ctx.channel());
            }
        }
        if (evt instanceof GlobalRatelimitEvent) {
            transportHandler.globalRateLimitReject();
        }
    }
}
