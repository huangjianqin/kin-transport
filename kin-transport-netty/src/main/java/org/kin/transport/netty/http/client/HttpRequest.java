package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.HttpMethod;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpUrl;

import java.util.Map;

/**
 * @author huangjianqin
 * @date 2020/9/2
 */
public final class HttpRequest implements HttpEntity {
    /** 请求的url信息 */
    private final HttpUrl url;
    /** 请求的method */
    private HttpMethod method;
    /** 请求的http头部信息 */
    private HttpHeaders headers;
    /** 请求的http内容 */
    private HttpRequestBody requestBody;

    private HttpRequest(HttpUrl url) {
        this.url = url;
        this.method = HttpMethod.GET;
        this.headers = new HttpHeaders();
    }

    //builder
    public static HttpRequest of(String url) {
        return of(HttpUrl.of(url));
    }

    public static HttpRequest of(HttpUrl httpUrl) {
        return new HttpRequest(httpUrl);
    }

    private HttpRequest request(HttpMethod method, HttpRequestBody requestBody) {
        this.method = method;
        this.requestBody = requestBody;
        return this;
    }

    public HttpRequest get() {
        return request(HttpMethod.GET, null);
    }

    public HttpRequest post() {
        return post(null);
    }

    public HttpRequest post(HttpRequestBody requestBody) {
        return request(HttpMethod.POST, requestBody);
    }

    public HttpRequest delete() {
        return delete(null);
    }

    public HttpRequest delete(HttpRequestBody requestBody) {
        return request(HttpMethod.DELETE, requestBody);
    }

    public HttpRequest put() {
        return put(null);
    }

    public HttpRequest put(HttpRequestBody requestBody) {
        return request(HttpMethod.PUT, requestBody);
    }

    public HttpRequest header(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpRequest headers(Map<String, String> kvs) {
        headers.putAll(kvs);
        return this;
    }

    //-------------------------------------------------getter----------------------------------------------------------------
    public HttpUrl getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpRequestBody getRequestBody() {
        return requestBody;
    }
}
