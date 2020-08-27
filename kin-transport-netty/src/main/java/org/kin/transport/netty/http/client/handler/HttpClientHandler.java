package org.kin.transport.netty.http.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.http.client.HttpClientTransportHandler;
import org.kin.transport.netty.utils.ChannelUtils;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private final HttpClientTransportHandler transportHandler;

    public HttpClientHandler() {
        this(null);
    }

    public HttpClientHandler(HttpClientTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        if (Objects.nonNull(transportHandler)) {
            transportHandler.handle(ctx, response);
        } else {
            ctx.fireChannelRead(response.content());
        }
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
