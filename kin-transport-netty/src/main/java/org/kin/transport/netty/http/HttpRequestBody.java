package org.kin.transport.netty.http;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;

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
    private ByteBuffer buf;
    /** media类型 */
    private MediaTypeWrapper mediaTypeWrapper;

    private HttpRequestBody() {
    }

    //-------------------------------------------------------------------------------------------------------------
    public static HttpRequestBody of(ByteBuffer byteBuffer, MediaTypeWrapper mediaTypeWrapper) {
        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.buf = byteBuffer;
        requestBody.mediaTypeWrapper = mediaTypeWrapper;
        ByteBufferUtils.toReadMode(requestBody.buf);
        return requestBody;
    }

    public static HttpRequestBody of(ByteBuf byteBuf, MediaTypeWrapper mediaTypeWrapper) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(byteBuffer);
        return of(byteBuffer, mediaTypeWrapper);
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 获取content string
     */
    public String getContent() {
        return mediaTypeWrapper.mediaType().parseContent(buf, mediaTypeWrapper.rawCharset());
    }

    /**
     * 将content转换成map参数并返回
     */
    public Map<String, Object> getParams() {
        return mediaTypeWrapper.mediaType().parseParams(buf, mediaTypeWrapper.rawCharset());
    }

    //-------------------------------------------------------------------------------------------------------------
    public ByteBuffer getBuf() {
        return buf;
    }

    public MediaTypeWrapper getMediaType() {
        return mediaTypeWrapper;
    }
}
