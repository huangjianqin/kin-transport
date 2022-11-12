package org.kin.transport.netty.http;

import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.http.server.HandlerInterceptor;
import org.kin.transport.netty.http.server.HttpRequestHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * @author huangjianqin
 * @date 2022/11/10
 */
public class LimitInterceptor implements HandlerInterceptor {
    @Override
    public Publisher<Void> preHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        System.out.println(Thread.currentThread().getName());
        System.out.println("pre http request handle");
        String limit = request.requestHeaders().get("limit");
        if (StringUtils.isNotBlank(limit) && Boolean.parseBoolean(limit)) {
            return response.sendString(Mono.just("server limit"));
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
