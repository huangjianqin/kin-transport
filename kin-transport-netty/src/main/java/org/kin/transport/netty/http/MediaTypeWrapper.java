package org.kin.transport.netty.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

    public MediaTypeWrapper(String mediaType) {
        this(mediaType, StandardCharsets.UTF_8.name());
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 从content type解析出media type
     */
    public static MediaTypeWrapper parse(String contentType) {
        String[] splits = contentType.split(";");
        //解析content-type
        String mediaType = splits[0].trim().toLowerCase();

        //解析charset
        for (int i = 1; i < splits.length; i++) {
            String[] itemSplits = splits[i].split("=");
            if (itemSplits[0].equals("charset")) {
                return new MediaTypeWrapper(mediaType, itemSplits[1]);
            }
        }

        return new MediaTypeWrapper(mediaType);
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
