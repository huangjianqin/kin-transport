package org.kin.transport.netty.http.client;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 获取并初始化http client的interceptor
 *
 * @author huangjianqin
 * @date 2020/9/8
 */
class ConnectInterceptor implements Interceptor {
    public static final ConnectInterceptor INSTANCE = new ConnectInterceptor();

    private ConnectInterceptor() {
    }

    @Override
    public HttpResponse intercept(HttpInterceptorChain chain) throws IOException {
        HttpCall httpCall = chain.httpCall();
        KinHttpClient kinHttpClient = httpCall.kinHttpClient();
        InetSocketAddress address = httpCall.httpRequest().url().address();

        return chain.proceed(httpCall.httpRequest(), kinHttpClient.client(address));
    }
}
