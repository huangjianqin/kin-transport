package org.kin.transport.netty.http.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class ByteBuf2HttpRequestEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        //TODO
        URI url;
        try {
            url = new URI("/test");
        } catch (URISyntaxException e) {
            ctx.fireExceptionCaught(e);
            return;
        }
        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0, HttpMethod.GET, url.toASCIIString(), msg);

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        ctx.writeAndFlush(request);
    }
}


