package org.kin.transport.netty.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * media type的封装
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public final class MediaTypeWrapper {
    /** 类型 */
    private final String mediaType;
    /** 编码 */
    private final String charset;

    public MediaTypeWrapper(String mediaType, String charset) {
        this.mediaType = mediaType;
        this.charset = charset;
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 从content type解析出media type
     */
    public static MediaTypeWrapper parse(String contentType) {
        int index1 = contentType.lastIndexOf(";");
        String mediaType = contentType.substring(0, index1).trim();

        String charsetStr = contentType.substring(index1 + 1, contentType.length()).trim();
        int index2 = charsetStr.lastIndexOf("=");
        String charset = charsetStr.substring(index2 + 1, charsetStr.length());
        return new MediaTypeWrapper(mediaType, charset);
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 获取Charset
     */
    public Charset charset() {
        return Charset.forName(charset);
    }

    /**
     * 根据指定Charset解码特定ByteBuffer内容
     */
    public String transfer(ByteBuffer source) {
        return charset().decode(source).toString();
    }

    /**
     * 转换成content type str
     */
    public String toContentType() {
        return mediaType.concat(";charset=").concat(charset);
    }

    public MediaType mediaType() {
        return MediaType.getByDesc(mediaType);
    }

    //-------------------------------------------------------------------------------------------------------------
    public String rawMediaType() {
        return mediaType;
    }

    public String rawCharset() {
        return charset;
    }
}
