package org.kin.transport.netty.http;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * http 请求内容
 *
 * @author huangjianqin
 * @date 2020/9/2
 */
public final class HttpRequestBody {
    /** request内容, 仅仅被消费一次 */
    private ByteBuffer sink;
    /** media类型 */
    private MediaTypeWrapper mediaTypeWrapper;

    private HttpRequestBody() {
    }

    //-------------------------------------------------------------------------------------------------------------
    public static HttpRequestBody of(String content, MediaTypeWrapper mediaTypeWrapper) {
        HttpRequestBody requestBody = new HttpRequestBody();
        byte[] bytes = content.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length).put(bytes);
        byteBuffer.flip();

        requestBody.sink = byteBuffer;
        requestBody.mediaTypeWrapper = mediaTypeWrapper;
        return requestBody;
    }

    public static HttpRequestBody of(ByteBuffer byteBuffer, MediaTypeWrapper mediaTypeWrapper) {
        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.sink = byteBuffer;
        requestBody.mediaTypeWrapper = mediaTypeWrapper;
        return requestBody;
    }

    public static HttpRequestBody of(ByteBuf byteBuf, MediaTypeWrapper mediaTypeWrapper) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(byteBuffer);
        return of(byteBuffer, mediaTypeWrapper);
    }

    //-------------------------------------------------------------------------------------------------------------
    public String getContent() {
        return mediaTypeWrapper.mediaType().parseContent(sink, mediaTypeWrapper.rawCharset());
    }

    public Map<String, Object> getParams() {
        return mediaTypeWrapper.mediaType().parseParams(sink, mediaTypeWrapper.rawCharset());
    }

    //-------------------------------------------------------------------------------------------------------------
    public ByteBuffer getSink() {
        return sink;
    }

    public MediaTypeWrapper getMediaType() {
        return mediaTypeWrapper;
    }
}
