package org.kin.transport.netty.websocket.client.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import org.kin.framework.log.LoggerOprs;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsClientHandler extends SimpleChannelInboundHandler<Object> implements LoggerOprs {
    /** ws握手 */
    private final WebSocketClientHandshaker handshaker;
    /** ws握手future */
    private ChannelPromise handshakeFuture;

    public WsClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                //握手成功
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                log().info("websocket client handshake success");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log().error("websocket client handshake failure");
                handshakeFuture.setFailure(e);
                //握手失败, 关闭channel
                ctx.channel().close();
                //后续针对channel失效处理, 可能是重连
                ctx.fireChannelInactive();
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            log().error("Unexpected FullHttpResponse (getStatus={}, content={})",
                    response.status(), response.content().toString(CharsetUtil.UTF_8));
            return;
        }

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
