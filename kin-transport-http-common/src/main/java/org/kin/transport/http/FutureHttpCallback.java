package org.kin.transport.http;

import java.util.concurrent.CompletableFuture;

/**
 * 触发{@link CompletableFuture}的{@link HttpCallback}实现
 *
 * @author huangjianqin
 * @date 2021/12/30
 */
public final class FutureHttpCallback<T> implements HttpCallback<T> {
    private final CompletableFuture<HttpResponse> future;

    public FutureHttpCallback() {
        this(new CompletableFuture<>());
    }

    public FutureHttpCallback(CompletableFuture<HttpResponse> future) {
        this.future = future;
    }

    @Override
    public void onReceived(HttpResponse response) {
        future.complete(response);
    }

    @Override
    public void onFailure(Throwable exception) {
        future.completeExceptionally(exception);
    }

    //getter
    public CompletableFuture<HttpResponse> getFuture() {
        return future;
    }
}
