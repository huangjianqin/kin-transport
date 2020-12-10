package org.kin.transport.netty.http.client;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.http.server.ServletException;

import java.io.IOException;

/**
 * http call重试interceptor
 *
 * @author huangjianqin
 * @date 2020/9/9
 */
class RetryCallInterceptor implements Interceptor {
    /** 最大重试次数 */
    private final int maxRetry;
    /** 当前尝试次数 */
    private int nowTry;

    RetryCallInterceptor(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    @Override
    public HttpResponse intercept(HttpInterceptorChain chain) throws IOException {
        while ((nowTry < maxRetry) || maxRetry == -1) {
            //仍然有尝试次数
            try {
                return chain.proceed(chain.getCall().getRequest());
            } catch (Exception e) {
                ExceptionUtils.throwExt(e);
            }
        }
        throw new ServletException(String.format("http call fail with retry %s times", nowTry));
    }
}
