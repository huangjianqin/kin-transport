package org.kin.transport.netty.http.server;

import org.kin.framework.Closeable;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

import java.util.Objects;

/**
 * http server transport启动后的返回值, 用于外部关闭http server
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class HttpServer implements Closeable {
    private volatile DisposableServer disposable;

    public HttpServer(Mono<DisposableServer> disposableMono) {
        //这里才subscribe, 真正启动http server
        disposableMono.doOnNext(d -> disposable = d).subscribe();
    }

    /**
     * 阻塞
     */
    public void block() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (Objects.isNull(disposable)) {
            return;
        }

        disposable.dispose();
    }
}
