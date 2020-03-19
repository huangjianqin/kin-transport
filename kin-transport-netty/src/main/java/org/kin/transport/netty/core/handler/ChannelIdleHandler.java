package org.kin.transport.netty.core.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.core.TransportHandler;

/**
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelIdleHandler extends ChannelDuplexHandler {
    private TransportHandler transportHandler;

    public ChannelIdleHandler() {
    }

    public ChannelIdleHandler(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
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
    }
}
