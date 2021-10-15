package org.kin.transport.netty.http.client;


import org.kin.framework.concurrent.OneLock;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * http call future
 *
 * @author huangjianqin
 * @date 2021/1/24
 */
public class HttpCallFuture implements Future<HttpResponse> {
    /** http call */
    private final HttpCall httpCall;
    /** 一次性锁 */
    private final OneLock sync;
    /** http response */
    private volatile HttpResponse httpResponse;
    /** future cancelled 标识 */
    private final AtomicBoolean cancelled = new AtomicBoolean();
    /** 该future绑定的http 传输链接 */
    private final HttpClient httpClient;

    public HttpCallFuture(HttpCall httpCall, HttpClient httpClient) {
        this.httpCall = httpCall;
        this.httpClient = httpClient;
        this.sync = new OneLock();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isDone() && cancelled.compareAndSet(false, true)) {
            //仅仅释放锁
            sync.release(1);
            return true;
        }

        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public boolean isDone() {
        return sync.isDone() || isCancelled();
    }

    @Override
    public HttpResponse get() {
        //获取锁
        sync.acquire(-1);
        if (isDone()) {
            return httpResponse;
        }
        return null;
    }

    @Override
    public HttpResponse get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (isDone()) {
                return httpResponse;
            }
            return null;
        }
        throw new TimeoutException();
    }

    synchronized void done(HttpResponse httpResponse) {
        if (isDone()) {
            return;
        }
        httpClient.free();
        //空即retry
        this.httpResponse = httpResponse;
        if (Objects.nonNull(this.httpResponse)) {
            this.httpResponse.setHttpRequest(httpCall.getRequest());
        }
        //释放锁
        sync.release(1);
    }
}
