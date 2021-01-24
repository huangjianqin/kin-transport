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
        HttpCall httpCall = chain.getCall();
        KinHttpClient kinHttpClient = httpCall.getHttpClient();
        InetSocketAddress address = httpCall.getRequest().getUrl().address();

        HttpClient httpClient = kinHttpClient.client(address);
        try {
            return chain.proceed(httpCall.getRequest(), httpClient);
        } finally {
            //归还已使用的client
            kinHttpClient.returnClient(address, httpClient);
        }
    }
}
