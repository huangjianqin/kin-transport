package org.kin.transport.netty.http.client;

import org.kin.framework.utils.ExceptionUtils;

import java.io.IOException;
import java.util.Objects;

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
                HttpResponse httpResponse = chain.proceed(chain.getCall().getRequest());
                if (Objects.nonNull(httpResponse) && httpResponse.isSuccess()) {
                    //成功返回
                    return httpResponse;
                }
            } catch (Exception e) {
                ExceptionUtils.throwExt(e);
            }
            nowTry++;
        }
        throw new RuntimeException(String.format("http call fail with retry %s times", nowTry));
    }
}
