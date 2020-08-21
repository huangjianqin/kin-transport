package org.kin.transport.netty.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.framework.log.LoggerOprs;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsServerhandler extends SimpleChannelInboundHandler<WebSocketFrame> implements LoggerOprs {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof BinaryWebSocketFrame) {
            ctx.fireChannelRead(msg.content());
        } else if (msg instanceof PongWebSocketFrame) {
            log().debug("websocket server received pong");
        } else if (msg instanceof CloseWebSocketFrame) {
            log().info("websocket server received closing");
            ctx.channel().close();
            ctx.fireChannelInactive();
        } else {
            //其他类型的的websocket frame, 需要自己新增处理器处理
            ctx.fireChannelRead(msg);
        }
    }
}
