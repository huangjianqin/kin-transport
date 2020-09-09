package org.kin.transport.netty.http.client;

import java.nio.ByteBuffer;

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

    //-------------------------------------------------------------------------------------------------------------
    public ByteBuffer sink() {
        return sink;
    }

    public MediaTypeWrapper mediaType() {
        return mediaTypeWrapper;
    }
}
