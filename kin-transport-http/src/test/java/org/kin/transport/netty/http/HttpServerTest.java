package org.kin.transport.netty.http;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.http.server.HttpServer;
import org.kin.transport.netty.http.server.HttpServerTransport;
import reactor.core.publisher.Mono;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class HttpServerTest {
    public static void main(String[] args) throws InterruptedException {
        HttpServer httpServer = HttpServerTransport.create()
                .mapping(new PrintController())
//                .threadCap()
                .interceptor(new LimitInterceptor())
                .interceptor(new RedirectInterceptor())
                .doOnException(UnsupportedOperationException.class, (req, t) -> Mono.just(ExceptionUtils.getExceptionDesc(t)))
                .bind();

        Runtime.getRuntime().addShutdownHook(new Thread(httpServer::close));
        Thread.currentThread().join();
//        Thread.sleep(5_000);
//        httpServer.close();
    }
}
