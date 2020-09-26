package org.kin.transport.netty.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.stream.ChunkedStream;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.MediaTypeWrapper;
import org.kin.transport.netty.http.client.HttpHeaders;

import java.io.ByteArrayInputStream;
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
    /** response内容大小, 如果大于10m, 则采用chunked write, 否则直接write full response todo 测试1k */
    private static final int CONTENT_SIZE_LIMIT = 1024 * 1;
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

        HttpResponseBody responseBody = servletResponse.getResponseBody();
        int contentSize = 0;
        if (Objects.nonNull(responseBody)) {
            contentSize = responseBody.contentSize();
        }

        if (contentSize >= CONTENT_SIZE_LIMIT) {
            //write chunked
            writeChunkedResponse(ctx, servletResponse);
        } else {
            //write full response
            writeFullResponse(ctx, servletResponse);
        }

        return Collections.emptyList();
    }

    /**
     * 直接write full response
     */
    private void writeFullResponse(ChannelHandlerContext ctx, ServletResponse servletResponse) {
        HttpResponseBody responseBody = servletResponse.getResponseBody();
        ByteBuf byteBuf = Unpooled.EMPTY_BUFFER;
        if (Objects.nonNull(responseBody)) {
            byteBuf.writeBytes(responseBody.bytes());
        }

        HttpUrl httpUrl = servletResponse.getUrl();
        boolean keepAlive = servletResponse.isKeepAlive();

        HttpVersion httpVersion = httpUrl.getVersion();
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.valueOf(servletResponse.getStatusCode()),
                byteBuf);

        for (Map.Entry<String, String> entry : response.headers()) {
            response.headers().set(entry.getKey(), entry.getValue());
        }

        if (Objects.nonNull(responseBody)) {
            response.headers()
                    .set(CONTENT_TYPE, responseBody.getMediaType().toContentType())
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());
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
    }

    private void writeChunkedResponse(ChannelHandlerContext ctx, ServletResponse servletResponse) {
        HttpUrl httpUrl = servletResponse.getUrl();
        boolean keepAlive = servletResponse.isKeepAlive();

        HttpVersion httpVersion = httpUrl.getVersion();
        HttpResponse response = new DefaultHttpResponse(httpVersion, HttpResponseStatus.valueOf(servletResponse.getStatusCode()));

        for (Map.Entry<String, String> entry : response.headers()) {
            response.headers().set(entry.getKey(), entry.getValue());
        }

        HttpResponseBody responseBody = servletResponse.getResponseBody();
        byte[] contentBytes = responseBody.bytes();
        if (Objects.nonNull(responseBody)) {
            response.headers()
                    .set(CONTENT_TYPE, responseBody.getMediaType().toContentType())
                    .setInt(CONTENT_LENGTH, contentBytes.length);
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

        //write response head
        ctx.write(response);

        //write the content
        ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
        ChannelFuture writeLastContentFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(inputStream)),
                ctx.newProgressivePromise());

        writeLastContentFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                Channel channel = future.channel();
                if (total < 0) {
                    log().debug("{}({}) Transfer progress:{}", channel, servletResponse, progress);
                } else {
                    log().debug("{}({}) Transfer progress:{}/{}", channel, servletResponse, progress, total);
                }
            }

            @Override
            public void operationComplete(ChannelProgressiveFuture future) {
                log().debug("{}({}) Transfer complete.", future.channel(), servletResponse);
            }
        });

        if (!keepAlive) {
            writeLastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

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
