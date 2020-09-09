package org.kin.transport.netty.http.server;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
import org.kin.transport.netty.utils.ChannelUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * 基于{@link SocketTransfer}
 *
 * @author huangjianqin
 * @date 2020/8/31
 */
public class HttpServerbinaryTransfer
        extends AbstractTransportProtocolTransfer<FullHttpRequest, SocketProtocol, FullHttpResponse>
        implements LoggerOprs {
    private final SocketTransfer transfer;
    /** 限流 */
    private final RateLimiter globalRateLimiter;

    public HttpServerbinaryTransfer(boolean compression, int globalRateLimit) {
        super(compression);
        this.transfer = new SocketTransfer(compression, true);
        if (globalRateLimit > 0) {
            globalRateLimiter = RateLimiter.create(globalRateLimit);
        } else {
            globalRateLimiter = null;
        }
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (ChannelUtils.globalRateLimit(ctx, globalRateLimiter)) {
            return Collections.emptyList();
        }

        Channel channel = ctx.channel();
        HttpSession.put(channel, request);

        return transfer.decode(ctx, request.content());
    }

    @Override
    public Collection<FullHttpResponse> encode(ChannelHandlerContext ctx, SocketProtocol protocol) throws Exception {
        ByteBuf protocolByteBuf = protocol.write().getByteBuf();
        ByteBuf byteBuf = ctx.alloc().buffer(protocolByteBuf.readableBytes() + 1);
        byteBuf.writeBoolean(compression);
        byteBuf.writeBytes(protocolByteBuf);

        Channel channel = ctx.channel();
        HttpSession httpSession = HttpSession.remove(channel);
        if (Objects.isNull(httpSession)) {
            log().error("no http session >>> {}", channel);
            return Collections.emptyList();
        }

        FullHttpRequest request = httpSession.getRequest();
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), OK,
                byteBuf);
        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, byteBuf.readableBytes());

        if (keepAlive) {
            if (!request.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(CONNECTION, CONNECTION);
            }
        } else {
            // 通知client关闭连接
            response.headers().set(CONNECTION, CLOSE);
        }

        ChannelFuture f = ctx.write(response);

        if (!keepAlive) {
            //TODO
            f.addListener(ChannelFutureListener.CLOSE);
        }
        return null;
    }

    @Override
    public Class<FullHttpRequest> getInClass() {
        return FullHttpRequest.class;
    }

    @Override
    public Class<SocketProtocol> getMsgClass() {
        return SocketProtocol.class;
    }
}
