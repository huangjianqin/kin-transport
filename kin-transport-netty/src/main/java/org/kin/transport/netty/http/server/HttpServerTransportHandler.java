package org.kin.transport.netty.http.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.kin.transport.netty.TransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public abstract class HttpServerTransportHandler extends TransportHandler<FullHttpRequest> {
    @Override
    public final void channelActive(ChannelHandlerContext ctx) {
        super.channelActive(ctx);
        //do nothing
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) {
        super.channelInactive(ctx);
        //do nothing
    }

    @Override
    public final void readWriteIdle(ChannelHandlerContext ctx) {
        super.readWriteIdle(ctx);
        //do nothing
    }

    @Override
    public final void readIdle(ChannelHandlerContext ctx) {
        super.readIdle(ctx);
        //do nothing
    }

    @Override
    public final void writeIdel(ChannelHandlerContext ctx) {
        super.writeIdel(ctx);
        //do nothing
    }
}
