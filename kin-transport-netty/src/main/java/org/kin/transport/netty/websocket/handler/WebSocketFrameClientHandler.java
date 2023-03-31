package org.kin.transport.netty.websocket.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.kin.framework.log.LoggerOprs;

/**
 * websocket client端inbound handler
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
@ChannelHandler.Sharable
public class WebSocketFrameClientHandler extends ChannelInboundHandlerAdapter implements LoggerOprs {
    public static final WebSocketFrameClientHandler INSTANCE = new WebSocketFrameClientHandler();

    private WebSocketFrameClientHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) {
        if (in instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) in;
            log().error("unexpected FullHttpResponse (getStatus={}, content={})",
                    response.status(), response.content().toString(CharsetUtil.UTF_8));
            return;
        }

        if (in instanceof WebSocketFrame) {
            if (in instanceof PongWebSocketFrame) {
                log().debug("websocket client received pong");
                return;
            } else if (in instanceof CloseWebSocketFrame) {
                log().info("websocket client receive CloseWebSocketFrame, prepare to close");
                ctx.channel().close();
                return;
            } else if (in instanceof BinaryWebSocketFrame) {
                ctx.fireChannelRead(((BinaryWebSocketFrame) in).content());
                return;
            }
        }

        //其他类型的的frame
        ctx.fireChannelRead(in);
    }
}