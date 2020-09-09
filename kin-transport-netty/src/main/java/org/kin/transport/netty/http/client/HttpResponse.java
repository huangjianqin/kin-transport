package org.kin.transport.netty.http.client;

import java.util.Map;

/**
 * @author huangjianqin
 * @date 2020/9/2
 */
public final class HttpResponse implements HttpEntity {
    /** 对应http request */
    private HttpRequest httpRequest;
    /** http response内容 */
    private final HttpResponseBody responseBody;
    /** tips */
    private final String message;
    /** response code */
    private final int code;
    /** response headers */
    private HttpHeaders headers;

    public HttpResponse(HttpResponseBody responseBody, String message, int code) {
        this.responseBody = responseBody;
        this.message = message;
        this.code = code;
        this.headers = new HttpHeaders();
    }

    //-------------------------------------------------------------------------------------------------------------
    public static HttpResponse of(HttpResponseBody responseBody, String message, int code) {
        return new HttpResponse(responseBody, message, code);
    }

    public static HttpResponse of(HttpResponseBody responseBody, int code) {
        return new HttpResponse(responseBody, "", code);
    }

    //-------------------------------------------------------------------------------------------------------------
    public HttpResponse header(String name, String value) {
        headers.set(name, value);
        return this;
    }

    public HttpResponse headers(Map<String, String> kvs) {
        headers.putAll(kvs);
        return this;
    }

    /**
     * response是否成功
     */
    public boolean isSuccess() {
        return 200 <= code && code < 300;
    }

    //-------------------------------------------------------------------------------------------------------------
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpRequest httpRequest() {
        return httpRequest;
    }

    public HttpResponseBody responseBody() {
        return responseBody;
    }

    public String message() {
        return message;
    }

    public int code() {
        return code;
    }

    public HttpHeaders headers() {
        return headers;
    }
}
