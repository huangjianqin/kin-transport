package org.kin.transport.netty.http.client;

import org.kin.framework.concurrent.ExecutionContext;

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
public final class HttpCall {
    /** 异步执行http call线程池 */
    private static ExecutionContext executionContext = ExecutionContext.cache("kin-http-call");
    /** key -> http请求 value -> future */
    private static Map<HttpCall, Future<?>> futures = new ConcurrentHashMap<>();

    private final KinHttpClient kinHttpClient;
    private final HttpRequest httpRequest;
    private HttpResponse httpResponse;

    public HttpCall(KinHttpClient kinHttpClient, HttpRequest httpRequest) {
        this.kinHttpClient = kinHttpClient;
        this.httpRequest = httpRequest;
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * 真正处理http call逻辑
     */
    private HttpResponse realExecute1() {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(kinHttpClient.getInterceptors());
        //内部实现的interceptor
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
            throw new RuntimeException(e);
        }
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
