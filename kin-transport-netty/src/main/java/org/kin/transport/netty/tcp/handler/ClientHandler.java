package org.kin.transport.netty.tcp.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * client端inbound handler
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
@ChannelHandler.Sharable
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    public static final ClientHandler INSTANCE = new ClientHandler();

    private ClientHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            log.error("encounter I/O exception: {}, force to close channel: {}", ExceptionUtils.getExceptionDesc(cause), ctx.channel());
            ctx.close();
        } else if (cause instanceof TransportException) {
            log.error("encounter I/O exception: {}, force to close channel: {}", ExceptionUtils.getExceptionDesc(cause), ctx.channel());
            ctx.close();
        } else {
            log.error("encounter exception:", cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                throw new TransportException("channel read idle, " + ctx.channel());
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}
