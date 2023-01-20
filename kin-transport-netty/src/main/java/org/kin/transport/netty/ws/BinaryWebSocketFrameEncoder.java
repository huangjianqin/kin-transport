package org.kin.transport.netty.ws;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

/**
 * outbound {@link ByteBuf}转{@link BinaryWebSocketFrame}
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
@ChannelHandler.Sharable
public final class BinaryWebSocketFrameEncoder extends ChannelOutboundHandlerAdapter {
    public static final BinaryWebSocketFrameEncoder INSTANCE = new BinaryWebSocketFrameEncoder();

    private BinaryWebSocketFrameEncoder() {
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ctx.write(new BinaryWebSocketFrame((ByteBuf) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
