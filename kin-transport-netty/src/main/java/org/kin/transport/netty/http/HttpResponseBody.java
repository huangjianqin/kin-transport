package org.kin.transport.netty.http;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * http response 内容
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public final class HttpResponseBody {
    /** response内容, 仅仅consume 一次 */
    private ByteBuffer source;
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
        responseBody.source = source;
        responseBody.mediaTypeWrapper = mediaTypeWrapper;
        return responseBody;
    }
    //-------------------------------------------------------------------------------------------------------------

    /**
     * 将response内容转换成字节数组, 并返回
     */
    public byte[] bytes() {
        byte[] bytes = new byte[source.remaining()];
        source.get(bytes);
        return bytes;
    }

    /**
     * 将response内容转换成字符串, 并返回
     */
    public String str() {
        return mediaTypeWrapper.transfer(source);
    }

    //-------------------------------------------------------------------------------------------------------------
    public MediaTypeWrapper getMediaType() {
        return mediaTypeWrapper;
    }
}
