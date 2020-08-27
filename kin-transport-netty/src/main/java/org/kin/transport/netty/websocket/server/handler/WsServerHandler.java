package org.kin.transport.netty.websocket.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.utils.ChannelUtils;
import org.kin.transport.netty.websocket.WsTransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> implements LoggerOprs {
    /** transport handler */
    private final WsTransportHandler transportHandler;

    public WsServerHandler(WsTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof PongWebSocketFrame) {
            //TODO
            log().debug("websocket server received pong");
        } else if (msg instanceof CloseWebSocketFrame) {
            log().info("websocket server received closing");
            ctx.channel().close();
        } else {
            //其他类型的的websocket frame
            transportHandler.handle(ctx, msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        transportHandler.channelActive(ctx);

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        transportHandler.channelInactive(ctx);

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ChannelUtils.handleUserEvent(evt, ctx, transportHandler);

        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        transportHandler.handleException(ctx, cause);

        super.exceptionCaught(ctx, cause);
    }
}
