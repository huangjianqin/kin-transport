package org.kin.transport.netty.handler;

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.kin.framework.log.LoggerOprs;

import java.net.URI;

/**
 * websocket client channel handler
 * 主要处理握手以及数据库传递
 *
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WebSocketClientHandler extends ChannelInboundHandlerAdapter implements LoggerOprs {
    /** ws握手 */
    private final WebSocketClientHandshaker handshaker;
    /** ws握手future */
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(URI uri) {
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
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
        ChannelFuture channelFuture = handshaker.handshake(ctx.channel());
        if (!channelFuture.isDone()) {
            //握手成功前异常
            handshakeFuture.setFailure(channelFuture.cause());
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object in) {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                //握手成功
                handshaker.finishHandshake(ch, (FullHttpResponse) in);
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

        if (in instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) in;
            log().error("Unexpected FullHttpResponse (getStatus={}, content={})",
                    response.status(), response.content().toString(CharsetUtil.UTF_8));
            return;
        }

        if (in instanceof WebSocketFrame) {
            if (in instanceof PongWebSocketFrame) {
                log().debug("websocket client received pong");
            } else if (in instanceof CloseWebSocketFrame) {
                log().info("websocket client received closing");
                ctx.channel().close();
            } else if (in instanceof BinaryWebSocketFrame) {
                ctx.fireChannelRead(((BinaryWebSocketFrame) in).content());
            }
        }

        //其他类型的的frame
        ctx.fireChannelRead(in);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            //握手成功前异常
            handshakeFuture.setFailure(cause);
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }
}
