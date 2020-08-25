package org.kin.transport.netty.http.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.http.client.HttpClientTransportHandler;

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
            transportHandler.handle(ctx.channel(), response);
        } else {
            ctx.fireChannelRead(response.content());
        }
    }
}
