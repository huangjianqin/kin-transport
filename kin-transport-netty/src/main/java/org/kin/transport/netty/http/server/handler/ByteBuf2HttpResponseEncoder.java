package org.kin.transport.netty.http.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.http.server.session.HttpSession;

import java.util.List;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class ByteBuf2HttpResponseEncoder extends MessageToMessageEncoder<ByteBuf> implements LoggerOprs {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        Channel channel = ctx.channel();
        HttpSession httpSession = HttpSession.get(channel);
        if (Objects.isNull(httpSession)) {
            log().error("no http session >>> {}", channel);
            return;
        }

        FullHttpRequest request = httpSession.getRequest();
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), OK,
                msg);
        response.headers()
                .set(CONTENT_TYPE, TEXT_PLAIN)
                .setInt(CONTENT_LENGTH, msg.readableBytes());

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
    }
}

