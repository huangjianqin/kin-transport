package org.kin.transport.netty.http.client;

import org.kin.framework.utils.ExceptionUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 最终真正执行http请求的interceptor
 *
 * @author huangjianqin
 * @date 2020/9/8
 */
class CallServerInterceptor implements Interceptor {
    public static final CallServerInterceptor INSTANCE = new CallServerInterceptor();

    private CallServerInterceptor() {
    }

    @Override
    public HttpResponse intercept(HttpInterceptorChain chain) throws IOException {
        HttpCall httpCall = chain.getCall();
        HttpClient httpClient = chain.getClient();
        if (Objects.isNull(httpClient)) {
            throw new IllegalStateException("http client not init");
        }
        Future<HttpResponse> future = httpClient.request(httpCall);
        try {
            HttpResponse response = future.get();
            return response;
        } catch (InterruptedException e) {
            future.cancel(true);
        } catch (ExecutionException e) {
            ExceptionUtils.throwExt(e);
        }

        return null;
    }
}
