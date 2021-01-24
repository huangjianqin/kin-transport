package org.kin.transport.netty.http;

import com.fasterxml.jackson.core.type.TypeReference;
import org.kin.framework.utils.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

/**
 * media type
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public enum MediaType {
    /** json */
    JSON("application/json") {
        @Override
        public HttpRequestBody toRequestBody(Map<String, Object> params, String charset) {
            return toRequestBody(params, Charset.forName(charset));
        }

        @Override
        public HttpRequestBody toRequestBody(Map<String, Object> params, Charset charset) {
            return toRequestBody(org.kin.framework.utils.JSON.write(params), charset);
        }

        @Override
        public HttpResponseBody toResponseBody(Map<String, Object> params, String charset) {
            return toResponseBody(params, Charset.forName(charset));
        }

        @Override
        public HttpResponseBody toResponseBody(Map<String, Object> params, Charset charset) {
            return toResponseBody(org.kin.framework.utils.JSON.write(params), charset);
        }

        @Override
        public Map<String, Object> parseParams(ByteBuffer sink, String charset) {
            String content = parseContent(sink, charset);
            if (StringUtils.isBlank(content)) {
                return Collections.emptyMap();
            }
            return org.kin.framework.utils.JSON.read(content, StrObjMapTypeReference.INSTANCE);
        }
    },
    /** 表单 */
    FORM("application/x-www-form-urlencoded") {
        @Override
        public HttpRequestBody toRequestBody(Map<String, Object> params, String charset) {
            return toRequestBody(params, Charset.forName(charset));
        }

        @Override
        public HttpRequestBody toRequestBody(Map<String, Object> params, Charset charset) {
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sj.add(entry.getKey().concat("=").concat(entry.getValue().toString()));
            }
            return toRequestBody(sj.toString(), charset);
        }

        @Override
        public HttpResponseBody toResponseBody(Map<String, Object> params, String charset) {
            return toResponseBody(params, Charset.forName(charset));
        }

        @Override
        public HttpResponseBody toResponseBody(Map<String, Object> params, Charset charset) {
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                sj.add(entry.getKey().concat("=").concat(entry.getValue().toString()));
            }
            return toResponseBody(sj.toString(), charset);
        }

        @Override
        public Map<String, Object> parseParams(ByteBuffer sink, String charset) {
            String content = parseContent(sink, charset);
            if (StringUtils.isBlank(content)) {
                return Collections.emptyMap();
            }
            Map<String, Object> params = new HashMap<>();
            for (String s1 : content.split("&")) {
                String[] splits = s1.split("=");
                params.put(splits[0], splits[1]);
            }
            return params;
        }
    },
    /** 纯文本 */
    PLAIN_TEXT("text/plain") {
    },
    /** html */
    HTML("text/html") {
    };
    private String desc;

    MediaType(String desc) {
        this.desc = desc;
    }

    public MediaTypeWrapper transfer(String charset) {
        return new MediaTypeWrapper(getDesc(), charset);
    }

    /**
     * 根据类型转换成对应的{@link HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(Map<String, Object> params, String charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(Map<String, Object> params, Charset charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(String content, String charset) {
        return toRequestBody(content, Charset.forName(charset));
    }

    /**
     * 根据类型转换成对应的{@link HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(String content, Charset charset) {
        return HttpRequestBody.of(charset.encode(content), transfer(charset.name()));
    }

    /**
     * 解析出请求request参数
     */
    public Map<String, Object> parseParams(ByteBuffer sink, String charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 解析出请求request参数
     */
    public String parseContent(ByteBuffer sink, String charsetStr) {
        Charset charset = Charset.forName(charsetStr);
        return charset.decode(sink).toString();
    }

    /**
     * 根据类型转换成对应的{@link HttpResponseBody}
     */
    public HttpResponseBody toResponseBody(Map<String, Object> params, String charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link HttpResponseBody}
     */
    public HttpResponseBody toResponseBody(Map<String, Object> params, Charset charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link HttpResponseBody}
     */
    public HttpResponseBody toResponseBody(String content, String charset) {
        if (Objects.isNull(content)) {
            content = "";
        }
        return toResponseBody(content, Charset.forName(charset));
    }

    /**
     * 根据类型转换成对应的{@link HttpResponseBody}
     */
    public HttpResponseBody toResponseBody(String content, Charset charset) {
        if (Objects.isNull(content)) {
            content = "";
        }
        return HttpResponseBody.of(charset.encode(content), transfer(charset.name()));
    }

    public String getDesc() {
        return desc;
    }

    //----------------------------------------------------------------------------------------------------------------
    public static MediaType getByDesc(String type) {
        for (MediaType mediaType : values()) {
            if (mediaType.getDesc().trim().equals(type)) {
                return mediaType;
            }
        }

        throw new UnknownMediaTypeException(type);
    }

    //----------------------------------------------------------------------------------------------------------------
    private static class StrObjMapTypeReference extends TypeReference<Map<String, Object>> {
        static final StrObjMapTypeReference INSTANCE = new StrObjMapTypeReference();
    }
}
