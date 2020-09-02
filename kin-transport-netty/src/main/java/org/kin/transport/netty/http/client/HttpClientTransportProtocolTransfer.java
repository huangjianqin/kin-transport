package org.kin.transport.netty.http.client;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
import org.kin.transport.netty.utils.ChannelUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

/**
 * 基于{@link SocketTransportProtocolTransfer}
 *
 * @author huangjianqin
 * @date 2020/8/31
 */
public class HttpClientTransportProtocolTransfer extends AbstractTransportProtocolTransfer<FullHttpResponse, SocketProtocol, FullHttpRequest>
        implements LoggerOprs {
    private final SocketTransportProtocolTransfer transfer;
    /** 限流 */
    private final RateLimiter globalRateLimiter;

    public HttpClientTransportProtocolTransfer(boolean compression, int globalRateLimit) {
        super(compression);
        this.transfer = new SocketTransportProtocolTransfer(compression, false);
        if (globalRateLimit > 0) {
            globalRateLimiter = RateLimiter.create(globalRateLimit);
        } else {
            globalRateLimiter = null;
        }
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        if (ChannelUtils.globalRateLimit(ctx, globalRateLimiter)) {
            return Collections.emptyList();
        }
        return transfer.decode(ctx, response.content());
    }

    @Override
    public Collection<FullHttpRequest> encode(ChannelHandlerContext ctx, SocketProtocol protocol) throws Exception {
        //TODO
        URI url;
        try {
            url = new URI("/test");
        } catch (URISyntaxException e) {
            ctx.fireExceptionCaught(e);
            return Collections.emptyList();
        }

        ByteBuf protocolByteBuf = protocol.write().getByteBuf();
        ByteBuf byteBuf = ctx.alloc().buffer(protocolByteBuf.readableBytes() + 1);
        byteBuf.writeBoolean(compression);
        byteBuf.writeBytes(protocolByteBuf);

        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0, HttpMethod.GET, url.toASCIIString(), byteBuf);

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        return Collections.singletonList(request);
    }

    @Override
    public Class<FullHttpResponse> getInClass() {
        return FullHttpResponse.class;
    }

    @Override
    public Class<SocketProtocol> getMsgClass() {
        return SocketProtocol.class;
    }

}
