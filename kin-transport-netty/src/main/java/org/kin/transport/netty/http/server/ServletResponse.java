package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.kin.framework.io.ByteBufferOutputStream;
import org.kin.framework.io.ByteBufferUtils;
import org.kin.transport.http.HttpCode;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.MediaType;
import org.kin.transport.netty.http.client.HttpHeaders;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class ServletResponse implements ServletTransportEntity {
    /** 请求的url信息 */
    private final HttpUrl url;
    /** 服务端处理状态码 */
    private int statusCode;
    /** 请求的http头部信息 */
    private final HttpHeaders headers = new HttpHeaders();
    /** cookies */
    private final List<Cookie> cookies;
    /** 是否长连接 */
    private final boolean isKeepAlive;
    /** 响应内容 */
    private HttpResponseBody responseBody;
    /** 响应内容的stream */
    private OutputStream outputStream;

    public ServletResponse(HttpUrl url, List<Cookie> cookies, boolean isKeepAlive) {
        this.url = url;
        this.cookies = cookies;
        this.isKeepAlive = isKeepAlive;
    }

    /**
     * 添加cookie
     */
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    /**
     * 重定向
     */
    public void sendRedirect(String location) {
        try {
            //clean buff
            responseBody = null;
            headers.add(HttpHeaderNames.LOCATION.toString(), location);
            statusCode = HttpCode.SC_FOUND;
        } catch (Exception e) {
            statusCode = HttpCode.SC_NOT_FOUND;
        }
    }

    public OutputStream getOutputStream() {
        if (Objects.isNull(outputStream)) {
            responseBody = null;
            //create new default 1M
            outputStream = new AutoExpandByteBufferOutputStream();
        }

        return outputStream;
    }

    //------------------------------------------------------------------------------------------------------------------------

    /**
     * 支持自动扩容的ByteBufferOutputStream
     * 一次写入的bytes不能超过1M
     */
    private class AutoExpandByteBufferOutputStream extends ByteBufferOutputStream {
        public AutoExpandByteBufferOutputStream() {
            super(1024 * 1024);
        }

        public AutoExpandByteBufferOutputStream(ByteBuffer sink) {
            super(sink);
            //仅仅继承, 但本质上不支持
            throw new UnsupportedOperationException();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            ByteBuffer sink = getSink();
            int sinkLimit = sink.limit();
            if (Objects.isNull(responseBody)) {
                responseBody = HttpResponseBody.of(ByteBuffer.allocate(sinkLimit), MediaType.HTML.transfer(StandardCharsets.UTF_8.name()));
            }

            ByteBuffer buf = responseBody.getBuf();
            ByteBuffer newBuf = null;
            ByteBufferUtils.toWriteMode(buf);
            while (buf.position() > 0 && buf.remaining() < sinkLimit) {
                int oldCapacity = buf.capacity();
                newBuf = ByteBuffer.allocate(Math.min(oldCapacity * 2, Integer.MAX_VALUE));

                ByteBufferUtils.copy(newBuf, buf);

                buf = newBuf;
            }

            if (sinkLimit > 0) {
                ByteBufferUtils.copyAndClear(buf, sink);
            }

            if (Objects.nonNull(newBuf)) {
                responseBody = HttpResponseBody.of(newBuf,
                        Objects.nonNull(responseBody) ? responseBody.getMediaType() : MediaType.HTML.transfer(StandardCharsets.UTF_8.name()));
            } else {
                ByteBufferUtils.toReadMode(buf);
            }
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            int bytesLen = length - offset;
            int streamBufferSize = getSink().capacity();
            if (bytesLen > streamBufferSize) {
                throw new IllegalArgumentException(String.format("write bytes length(%sB) greater than stream buffer size(%sB)", bytesLen, streamBufferSize));
            }
            super.write(bytes, offset, length);
        }
    }

    //setter && getter
    public HttpUrl getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public HttpResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(HttpResponseBody responseBody) {
        this.responseBody = responseBody;
    }
}
