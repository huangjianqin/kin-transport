package org.kin.transport.netty.http.client;

import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.SysUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * http请求封装
 *
 * @author huangjianqin
 * @date 2020/9/2
 */
public final class HttpCall implements LoggerOprs {
    /** 异步执行http call线程池 */
    private static ExecutionContext executionContext = ExecutionContext.elastic(SysUtils.CPU_NUM, SysUtils.CPU_NUM * 10, "kin-http-call");
    /** key -> http请求 value -> future */
    private static Map<HttpCall, Future<?>> futures = new ConcurrentHashMap<>();

    private final KinHttpClient kinHttpClient;
    private final HttpRequest httpRequest;
    private HttpResponse httpResponse;

    public HttpCall(KinHttpClient kinHttpClient, HttpRequest httpRequest) {
        this.kinHttpClient = kinHttpClient;
        this.httpRequest = httpRequest;
        //默认都是使用长连接, 不然底层传输channel无法复用
        this.httpRequest.header("connection", "keep-alive");
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 真正处理http call逻辑
     */
    private HttpResponse realExecute1() {
        List<Interceptor> interceptors = new ArrayList<>(kinHttpClient.getInterceptors());
        //内部实现的interceptor
        interceptors.add(CookieInterceptor.INSTANCE);
        interceptors.add(CacheInterceptor.INSTANCE);
        interceptors.add(new RetryCallInterceptor(kinHttpClient.getRetryTimes()));
        interceptors.add(ConnectInterceptor.INSTANCE);
        interceptors.add(CallServerInterceptor.INSTANCE);

        HttpInterceptorChain chain = new HttpInterceptorChain(interceptors, this, null, 0);
        httpResponse = chain.proceed(httpRequest);
        return httpResponse;
    }

    /**
     * 异步执行
     */
    private Future<HttpResponse> realExecute0(HttpCallback callback) {
        if (futures.containsKey(this)) {
            throw new IllegalStateException("async http call has been executed");
        }
        Future<HttpResponse> future = executionContext.submit(() -> {
            HttpResponse response = null;
            try {
                response = realExecute1();
                callback.onResponse(this, response);
            } catch (Exception e) {
                log().error("", e);
                callback.onFailure(this, e);
            }
            futures.remove(this);
            return response;
        });
        futures.put(this, future);
        return future;
    }

    /**
     * 立即调用
     */
    public HttpResponse execute() {
        try {
            Future<HttpResponse> future = realExecute0(EmptyHttpCallback.INSTANCE);
            long callTimeout = kinHttpClient.getCallTimeout();
            if (callTimeout > 0) {
                return future.get(callTimeout, TimeUnit.MILLISECONDS);
            } else {
                return future.get();
            }
        } catch (TimeoutException e) {
            //TODO http request 描述
            throw new HttpCallTimeoutException(httpRequest.toString());
        } catch (Exception e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("encounter unknown error");
    }

    /**
     * 异步调用
     */
    public void enqueue(HttpCallback callback) {
        realExecute0(callback);
    }

    /**
     * 取消call
     */
    public void cancel() {
        Future<?> future = futures.remove(this);
        if (Objects.nonNull(future)) {
            future.cancel(true);
        }
    }

    //-------------------------------------------------------------------------------------------------------------

    public KinHttpClient getHttpClient() {
        return kinHttpClient;
    }

    public HttpRequest getRequest() {
        return httpRequest;
    }

    public HttpResponse getResponse() {
        return httpResponse;
    }

    //-------------------------------------------------------------------------------------------------------------
}
