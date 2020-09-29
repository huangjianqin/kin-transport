package org.kin.transport.netty.http;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.ByteBufferUtils;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * http response 内容
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public final class HttpResponseBody {
    /** response内容, 一直处于read mode, 仅仅consume 一次 */
    private ByteBuffer buf;
    /** 类型 */
    private MediaTypeWrapper mediaTypeWrapper;

    //-------------------------------------------------------------------------------------------------------------
    public static HttpResponseBody of(ByteBuf byteBuf, MediaTypeWrapper mediaTypeWrapper) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(byteBuffer);
        return of(byteBuffer, mediaTypeWrapper);
    }

    public static HttpResponseBody of(ByteBuffer source, MediaTypeWrapper mediaTypeWrapper) {
        HttpResponseBody responseBody = new HttpResponseBody();
        responseBody.buf = source;
        responseBody.mediaTypeWrapper = mediaTypeWrapper;
        ByteBufferUtils.toReadMode(responseBody.buf);
        return responseBody;
    }
    //-------------------------------------------------------------------------------------------------------------

    /**
     * 将response内容转换成字节数组, 并返回
     */
    public byte[] bytes() {
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    public String str() {
        return mediaTypeWrapper.transfer(buf);
    }

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

    /**
     * response content size
     */
    public int contentSize() {
        return buf.remaining();
    }

    //-------------------------------------------------------------------------------------------------------------
    public MediaTypeWrapper getMediaType() {
        return mediaTypeWrapper;
    }

    public ByteBuffer getBuf() {
        return buf;
    }
}
