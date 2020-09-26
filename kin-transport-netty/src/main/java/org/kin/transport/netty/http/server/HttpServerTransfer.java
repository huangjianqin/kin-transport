package org.kin.transport.netty.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.MediaTypeWrapper;
import org.kin.transport.netty.http.client.HttpHeaders;

import java.util.*;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;

/**
 * @author huangjianqin
 * @date 2020/8/31
 */
class HttpServerTransfer
        extends AbstractTransportProtocolTransfer<FullHttpRequest, ServletTransportEntity, FullHttpResponse>
        implements LoggerOprs {
    /** cookie 解码 */
    private final ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
    /** cookie 编码 */
    private final ServerCookieEncoder cookieEncoder = ServerCookieEncoder.STRICT;


    public HttpServerTransfer(boolean compression) {
        super(compression);
    }

    @Override
    public Collection<ServletTransportEntity> decode(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, String> entry : request.headers()) {
            headers.add(entry.getKey().toLowerCase(), entry.getValue());
        }

        String contentType = headers.header(CONTENT_TYPE.toString());
        String cookieStr = headers.header(COOKIE.toString());

        List<Cookie> cookies = cookieDecoder.decode(cookieStr).stream().map(Cookie::of).collect(Collectors.toList());

        ServletRequest servletRequest = new ServletRequest(
                HttpUrl.of(request.uri(), request.protocolVersion()),
                request.method(),
                headers,
                cookies,
                StringUtils.isBlank(contentType) ? null : HttpRequestBody.of(request.content(), MediaTypeWrapper.parse(contentType)),
                HttpUtil.isKeepAlive(request)
        );

        return Collections.singleton(servletRequest);
    }

    @Override
    public Collection<FullHttpResponse> encode(ChannelHandlerContext ctx, ServletTransportEntity servletTransportEntity) throws Exception {
        if (!(servletTransportEntity instanceof ServletResponse)) {
            return Collections.emptyList();
        }
        ServletResponse servletResponse = (ServletResponse) servletTransportEntity;
        HttpUrl httpUrl = servletResponse.getUrl();
        boolean keepAlive = servletResponse.isKeepAlive();

        ByteBuf byteBuf = ctx.alloc().buffer();
        HttpResponseBody responseBody = servletResponse.getResponseBody();
        if (Objects.nonNull(responseBody)) {
            byteBuf.writeBytes(responseBody.bytes());
        }

        HttpVersion httpVersion = httpUrl.getVersion();
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.valueOf(servletResponse.getStatusCode()),
                byteBuf);

        for (Map.Entry<String, String> entry : response.headers()) {
            response.headers().set(entry.getKey(), entry.getValue());
        }

        if (Objects.nonNull(responseBody)) {
            response.headers()
                    .set(CONTENT_TYPE, responseBody.getMediaType().toContentType())
                    .setInt(CONTENT_LENGTH, byteBuf.readableBytes());
        }
        response.headers()
                .set(COOKIE, cookieEncoder.encode(servletResponse.getCookies().stream().map(Cookie::toNettyCookie).collect(Collectors.toList())))
                .set(SERVER, "kin-http-server");

        if (keepAlive) {
            if (!httpVersion.isKeepAliveDefault()) {
                response.headers().set(CONNECTION, CONNECTION);
            }
        } else {
            // 通知client关闭连接
            response.headers().set(CONNECTION, CLOSE);
        }

        ChannelFuture f = ctx.write(response);
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
        return Collections.emptyList();
    }

    @Override
    public Class<FullHttpRequest> getInClass() {
        return FullHttpRequest.class;
    }

    @Override
    public Class<ServletTransportEntity> getMsgClass() {
        return ServletTransportEntity.class;
    }
}
