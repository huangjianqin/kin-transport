package org.kin.transport.netty.http.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.MediaTypeWrapper;
import org.kin.transport.netty.socket.SocketTransfer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * 基于{@link SocketTransfer}
 *
 * @author huangjianqin
 * @date 2020/8/31
 */
class HttpClientTransfer extends AbstractTransportProtocolTransfer<FullHttpResponse, HttpEntity, FullHttpRequest>
        implements LoggerOprs {

    public HttpClientTransfer(boolean compression) {
        super(compression);
    }

    @Override
    public Collection<HttpEntity> decode(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        /**
         * 将 {@link FullHttpResponse} 转换成 {@link HttpResponse}
         */
        HttpResponseStatus responseStatus = response.status();
        int code = responseStatus.code();
        String message = responseStatus.reasonPhrase();
        String contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        HttpResponseBody responseBody = null;
        if (StringUtils.isNotBlank(contentType)) {
            responseBody = HttpResponseBody.of(response.content(), MediaTypeWrapper.parse(contentType));
        }

        HttpResponse httpResponse = HttpResponse.of(responseBody, message, code);
        for (Map.Entry<String, String> entry : response.headers().entries()) {
            httpResponse.headers().put(entry.getKey(), entry.getValue());
        }

        return Collections.singleton(httpResponse);
    }

    @Override
    public Collection<FullHttpRequest> encode(ChannelHandlerContext ctx, HttpEntity httpEntity) throws Exception {
        if (!(httpEntity instanceof HttpRequest)) {
            return Collections.emptyList();
        }

        HttpRequest httpRequest = (HttpRequest) httpEntity;
        HttpRequestBody requestBody = httpRequest.getRequestBody();
        ByteBuf content;
        if (Objects.nonNull(requestBody)) {
            //get时, body为空
            ByteBuffer byteBuffer = requestBody.getSink();
            content = ctx.alloc().buffer(byteBuffer.capacity());
            content.writeBytes(byteBuffer);
        } else {
            content = Unpooled.EMPTY_BUFFER;
        }

        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0,
                httpRequest.getMethod(),
                httpRequest.getUrl().uri().toASCIIString(),
                content);

        for (Map.Entry<String, String> entry : httpRequest.getHeaders().entrySet()) {
            request.headers().set(entry.getKey(), entry.getValue());
        }

        //设置content type
        if (Objects.nonNull(requestBody)) {
            //get时, body为空
            request.headers()
                    .set(HttpHeaderNames.CONTENT_TYPE, requestBody.getMediaType().toContentType())
                    .setInt(CONTENT_LENGTH, content.readableBytes());
        }

        return Collections.singletonList(request);
    }

    @Override
    public Class<FullHttpResponse> getInClass() {
        return FullHttpResponse.class;
    }

    @Override
    public Class<HttpEntity> getMsgClass() {
        return HttpEntity.class;
    }

}
