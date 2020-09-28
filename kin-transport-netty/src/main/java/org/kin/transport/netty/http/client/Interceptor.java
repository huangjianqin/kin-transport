package org.kin.transport.netty.http.client;

import java.io.IOException;

/**
 * http请求链-拦截器
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
@FunctionalInterface
public interface Interceptor {
    /**
     * 拦截逻辑
     *
     * @param chain
     * @return 有效的http response
     */
    HttpResponse intercept(HttpInterceptorChain chain) throws IOException;
}
