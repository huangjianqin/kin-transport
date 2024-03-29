package org.kin.transport.netty.http;

import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.http.server.HandlerInterceptor;
import org.kin.transport.netty.http.server.InterceptorChain;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * @author huangjianqin
 * @date 2022/11/10
 */
public class LimitInterceptor implements HandlerInterceptor {

    @Override
    public Mono<Void> preHandle(InterceptorChain chain) {
        HttpServerRequest request = chain.getRequest();
        HttpServerResponse response = chain.getResponse();

        System.out.println(Thread.currentThread().getName());
        System.out.println("pre http request handle");
        String limit = request.requestHeaders().get("limit");
        if (StringUtils.isNotBlank(limit) && Boolean.parseBoolean(limit)) {
            return response.sendString(Mono.just("server limit")).then();
        }
        return chain.next();
    }
}
