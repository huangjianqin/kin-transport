package org.kin.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.framework.log.LoggerOprs;

/**
 * websocket server端inbound handler
 *
 * @author huangjianqin
 * @date 2020/8/21
 */
@ChannelHandler.Sharable
public class WebSocketServerHandler extends ChannelInboundHandlerAdapter implements LoggerOprs {
    public static final WebSocketServerHandler INSTANCE = new WebSocketServerHandler();

    private WebSocketServerHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) throws Exception {
        if (in instanceof WebSocketFrame) {
            if (in instanceof CloseWebSocketFrame) {
                Channel channel = ctx.channel();
                if (channel.isActive()) {
                    log().info("websocket server received CloseWebSocketFrame");
                    channel.close();
                }
                return;
            }
            if (in instanceof BinaryWebSocketFrame) {
                //目前只接受binary data
                ctx.fireChannelRead(((BinaryWebSocketFrame) in).content());
                return;
            }
        }

        //其他类型的的frame
        ctx.fireChannelRead(in);
    }
}
