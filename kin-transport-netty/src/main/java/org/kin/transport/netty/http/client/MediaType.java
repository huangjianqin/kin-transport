package org.kin.transport.netty.http.client;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.StringJoiner;

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
            return HttpRequestBody.of(org.kin.framework.utils.JSON.write(params), transfer(charset));
        }

        @Override
        public HttpRequestBody toRequestBody(Map<String, Object> params, Charset charset) {
            return toRequestBody(params, charset.name());
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
            return HttpRequestBody.of(charset.encode(sj.toString()), transfer(charset.name()));
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

    protected MediaTypeWrapper transfer(String charset) {
        return new MediaTypeWrapper(getDesc(), charset);
    }

    /**
     * 根据类型转换成对应的{@link org.kin.transport.netty.http.client.HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(Map<String, Object> params, String charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link org.kin.transport.netty.http.client.HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(Map<String, Object> params, Charset charset) {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据类型转换成对应的{@link org.kin.transport.netty.http.client.HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(String content, String charset) {
        return HttpRequestBody.of(content, transfer(charset));
    }

    /**
     * 根据类型转换成对应的{@link org.kin.transport.netty.http.client.HttpRequestBody}
     */
    public HttpRequestBody toRequestBody(String content, Charset charset) {
        return toRequestBody(content, charset.name());
    }

    public String getDesc() {
        return desc;
    }
}
