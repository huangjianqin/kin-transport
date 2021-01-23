package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.client.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class ServletRequest implements ServletTransportEntity {
    /** 请求的url信息 */
    private final HttpUrl url;
    /** 请求的method */
    private final HttpMethod method;
    /** 请求的http头部信息 */
    private final HttpHeaders headers;
    /** http session */
    private HttpSession session;
    /** cookies */
    private final List<Cookie> cookies;
    /** request body */
    private final HttpRequestBody requestBody;
    /** 是否长连接 */
    private final boolean isKeepAlive;

    ServletRequest(HttpUrl url,
                   HttpMethod method,
                   HttpHeaders headers,
                   List<Cookie> cookies,
                   HttpRequestBody requestBody,
                   boolean isKeepAlive) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.cookies = cookies;
        this.requestBody = requestBody;
        this.isKeepAlive = isKeepAlive;
    }

    /**
     * 获取内容
     */
    public String getContent() {
        if (Objects.isNull(requestBody)) {
            return "";
        }
        return requestBody.getContent();
    }

    /**
     * 获取参数map
     */
    public Map<String, Object> getParams() {
        //默认先从url获取参数
        Map<String, Object> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(url.uri());
        for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
            //entry.getValue()是一个List, 只取第一个元素
            params.put(entry.getKey(), entry.getValue().get(0));
        }

        if (Objects.isNull(requestBody)) {
            return params;
        }

        //然后用body内容覆盖url的参数
        params.putAll(requestBody.getParams());
        return params;
    }

    /**
     * 获取cookie内容
     */
    public String cookie(String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    //getter
    public HttpUrl getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpSession getSession() {
        return session;
    }

    void setSession(HttpSession session) {
        this.session = session;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }
}
