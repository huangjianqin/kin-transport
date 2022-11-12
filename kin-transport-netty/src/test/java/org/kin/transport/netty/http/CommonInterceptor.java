package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.HandlerInterceptor;
import org.kin.transport.netty.http.server.HttpRequestHandler;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * @author huangjianqin
 * @date 2022/11/10
 */
public class CommonInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        //response只能send一次, 这里直接subscribe, 即通知response执行, 那么request handler的post handle之后定义的操作都无法执行
//        response.sendString(Mono.just("post handle test")).then().subscribe();
        System.out.println(Thread.currentThread().getName());
        System.out.println("post http request handle");
    }
}
