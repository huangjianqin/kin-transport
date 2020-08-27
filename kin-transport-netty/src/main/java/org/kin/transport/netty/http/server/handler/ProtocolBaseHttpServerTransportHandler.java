package org.kin.transport.netty.http.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.kin.transport.netty.http.server.HttpServerTransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpServerTransportHandler extends HttpServerTransportHandler {
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        ctx.fireChannelRead(request.content());
    }
}
