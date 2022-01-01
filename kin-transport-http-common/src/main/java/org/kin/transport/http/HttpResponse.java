package org.kin.transport.http;

/**
 * http response简单封装
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
public class HttpResponse {
    /**
     * 状态码
     *
     * @see HttpCode
     */
    private final int code;
    /**
     * http header
     */
    private final HttpHeaders headers;
    /**
     * http received data
     */
    private Object data;

    public HttpResponse(int code, HttpHeaders headers) {
        this.code = code;
        this.headers = headers;
    }

    //getter
    public int getCode() {
        return code;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }
}