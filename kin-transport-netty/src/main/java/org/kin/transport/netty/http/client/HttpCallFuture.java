package org.kin.transport.netty.http.client;


import java.util.Objects;
import java.util.concurrent.CountDownLatch;
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
    private final CountDownLatch latch = new CountDownLatch(1);
    /** http response */
    private volatile HttpResponse httpResponse;
    /** future完成标识 */
    private volatile boolean finished;
    /** future cancelled 标识 */
    private final AtomicBoolean cancelled = new AtomicBoolean();
    /** 该future绑定的http 传输链接 */
    private final HttpClient httpClient;

    public HttpCallFuture(HttpCall httpCall, HttpClient httpClient) {
        this.httpCall = httpCall;
        this.httpClient = httpClient;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isDone() && cancelled.compareAndSet(false, true)) {
            done0();
            //仅仅释放锁
            latch.countDown();
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
        return finished;
    }

    @Override
    public HttpResponse get() throws InterruptedException {
        //获取锁
        latch.await();
        if (isDone()) {
            return httpResponse;
        }
        return null;
    }

    @Override
    public HttpResponse get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        boolean success = latch.await(timeout, unit);
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
        done0();
        //空即retry
        this.httpResponse = httpResponse;
        if (Objects.nonNull(this.httpResponse)) {
            this.httpResponse.setHttpRequest(httpCall.getRequest());
        }
        //释放锁
        latch.countDown();
    }

    private void done0() {
        finished = true;
        httpClient.free();
    }
}
