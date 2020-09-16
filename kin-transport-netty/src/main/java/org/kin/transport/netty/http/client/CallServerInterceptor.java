package org.kin.transport.netty.http.client;

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
            throw new RuntimeException("http client not init");
        }
        Future<HttpResponse> future = httpClient.request(httpCall);
        try {
            return future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
