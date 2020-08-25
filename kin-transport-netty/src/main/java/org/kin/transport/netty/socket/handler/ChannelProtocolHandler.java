package org.kin.transport.netty.socket.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.domain.ProtocolRateLimiter;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.userevent.GlobalRatelimitEvent;
import org.kin.transport.netty.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 协议处理
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelProtocolHandler.class);
    private final TransportHandler<AbstractProtocol> transportHandler;

    public ChannelProtocolHandler(TransportHandler<AbstractProtocol> transportHandler) {
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
            log.debug("Recv {} {} {}", protocol.getProtocolId(), protocol.getClass().getSimpleName(), ChannelUtils.getRemoteIp(ctx.channel()));

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
        log.info("channel active: {}", ChannelUtils.getRemoteIp(channel));
        transportHandler.channelActive(ctx.channel());

        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel inactive: {}", ChannelUtils.getRemoteIp(channel));
        transportHandler.channelInactive(ctx.channel());

        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        log.error("server('{}') throw exception:{}", ChannelUtils.getRemoteIp(channel), cause);
        transportHandler.handleException(channel, cause);

        ctx.fireExceptionCaught(cause);
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

        ctx.fireUserEventTriggered(evt);
    }
}
