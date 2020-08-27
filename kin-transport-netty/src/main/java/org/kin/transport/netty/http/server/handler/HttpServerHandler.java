package org.kin.transport.netty.http.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.kin.transport.netty.http.server.HttpServerTransportHandler;
import org.kin.transport.netty.http.server.session.HttpSession;
import org.kin.transport.netty.utils.ChannelUtils;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final HttpServerTransportHandler transportHandler;

    public HttpServerHandler() {
        this(null);
    }

    public HttpServerHandler(HttpServerTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        Channel channel = ctx.channel();
        HttpSession.put(channel, request);

        if (Objects.nonNull(transportHandler)) {
            //自定义http请求包处理
            transportHandler.handle(ctx, request);
        } else {
            ctx.fireChannelRead(request.content());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        transportHandler.handleException(ctx, cause);

        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        HttpSession.remove(channel);

        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ChannelUtils.handleUserEvent(evt, ctx, transportHandler);

        super.userEventTriggered(ctx, evt);
    }
}
