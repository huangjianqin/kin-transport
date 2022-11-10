package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.HttpServer;
import org.kin.transport.netty.http.server.HttpServerTransport;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class HttpServerTest {
    public static void main(String[] args) throws InterruptedException {
        HttpServer httpServer = HttpServerTransport.create()
                .mapping(new PrintController())
                .bind();
        Runtime.getRuntime().addShutdownHook(new Thread(httpServer::close));
        Thread.currentThread().join();
    }
}
