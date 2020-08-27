package org.kin.transport.netty.http.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.http.client.HttpClientTransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpClientTransportHandler extends HttpClientTransportHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpResponse response) {
        ctx.fireChannelRead(response.content());
    }
}
