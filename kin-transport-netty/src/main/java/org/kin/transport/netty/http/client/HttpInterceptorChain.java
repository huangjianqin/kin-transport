package org.kin.transport.netty.http.client;

import org.kin.framework.utils.ExceptionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * http 调用链, 供interceptor使用
 * <p>
 * 责任链模式调用
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public final class HttpInterceptorChain {
    /** interceptors */
    private final List<Interceptor> interceptors;
    /** http call */
    private final HttpCall httpCall;
    /** http client */
    private final HttpClient httpClient;
    /** 当前interceptor 索引 */
    private final int index;

    public HttpInterceptorChain(List<Interceptor> interceptors, HttpCall httpCall, HttpClient httpClient, int index) {
        this.interceptors = interceptors;
        this.httpCall = httpCall;
        this.httpClient = httpClient;
        this.index = index;
    }

    /**
     * 执行interceptor链
     */
    public HttpResponse proceed(HttpRequest httpRequest) {
        return proceed(httpRequest, httpClient);
    }

    /**
     * 执行interceptor链
     */
    public HttpResponse proceed(HttpRequest httpRequest, HttpClient httpClient) {
        if (index >= interceptors.size()) {
            throw new IndexOutOfBoundsException("index greater than interceptors's size");
        }

        if (Objects.nonNull(httpClient) && Objects.nonNull(this.httpClient)) {
            throw new IndexOutOfBoundsException("connect interceptor execute exactly once");
        }

        int nextIndex = this.index + 1;
        HttpInterceptorChain nextChain = new HttpInterceptorChain(this.interceptors, this.httpCall, httpClient, nextIndex);
        Interceptor interceptor = interceptors.get(index);
        HttpResponse httpResponse = null;
        try {
            httpResponse = interceptor.intercept(nextChain);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
        }
        if (Objects.nonNull(httpResponse)) {
            if (Objects.isNull(httpResponse.responseBody())) {
                throw new NullPointerException(String.format("interceptor %s return a response with empty body", interceptor));
            }

            return httpResponse;
        } else {
            throw new NullPointerException(String.format("interceptor %s return null", interceptor));
        }
    }

    //-------------------------------------------------------------------------------------------------------------
    HttpCall getCall() {
        return httpCall;
    }

    HttpClient getClient() {
        return httpClient;
    }
}
