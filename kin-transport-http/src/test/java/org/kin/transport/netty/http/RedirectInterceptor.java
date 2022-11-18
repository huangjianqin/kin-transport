package org.kin.transport.netty.http;

import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.http.server.HandlerInterceptor;
import org.kin.transport.netty.http.server.InterceptorChain;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

/**
 * @author huangjianqin
 * @date 2022/11/18
 */
public class RedirectInterceptor implements HandlerInterceptor {
    @Override
    public Mono<Void> preHandle(InterceptorChain chain) {
        HttpServerRequest request = chain.getRequest();

        System.out.println(Thread.currentThread().getName());
        System.out.println("pre http request handle");
        String redirect = request.requestHeaders().get("redirect");
        if (StringUtils.isNotBlank(redirect)) {
            return chain.redirect(redirect);
        }
        return chain.next();
    }
}
