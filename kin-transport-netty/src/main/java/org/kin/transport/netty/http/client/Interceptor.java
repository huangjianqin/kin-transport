package org.kin.transport.netty.http.client;

import com.sun.istack.internal.NotNull;

import java.io.IOException;

/**
 * http请求链-拦截器
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
@FunctionalInterface
public interface Interceptor {
    HttpResponse intercept(@NotNull HttpInterceptorChain chain) throws IOException;
}
