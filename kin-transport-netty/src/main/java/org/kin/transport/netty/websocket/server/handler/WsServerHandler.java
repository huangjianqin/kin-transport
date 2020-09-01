package org.kin.transport.netty.websocket.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.framework.log.LoggerOprs;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsServerHandler extends ChannelInboundHandlerAdapter implements LoggerOprs {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            if (msg instanceof PongWebSocketFrame) {
                //TODO
                log().debug("websocket server received pong");
            } else if (msg instanceof CloseWebSocketFrame) {
                log().info("websocket server received closing");
                ctx.channel().close();
            } else {
                //其他类型的的websocket frame
                ctx.fireChannelRead(msg);
            }
        }
    }
}
