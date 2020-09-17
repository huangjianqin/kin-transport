package org.kin.transport.netty.http;

import org.kin.transport.netty.http.client.HttpRequest;
import org.kin.transport.netty.http.client.HttpResponse;
import org.kin.transport.netty.http.client.KinHttpClient;
import org.kin.transport.netty.http.server.KinHttpServer;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class HttpTest {
    public static void main(String[] args) throws InterruptedException {
        InetSocketAddress address = new InetSocketAddress(8880);
        KinHttpServer.builder().mappingServlet("/", PrintServlet.class).build(address);

        KinHttpClient kinHttpClient = KinHttpClient.builder();
        HttpRequest httpRequest = HttpRequest.of("http://127.0.0.1:8880/a").get();
        HttpResponse response = kinHttpClient.newCall(httpRequest).execute();
        System.out.println(response.code());
        System.out.println(response.headers());
        System.out.println(response.message());
//        System.out.println(response.responseBody().getParams());
        System.out.println(response.responseBody().getContent());

        Thread.sleep(5000);
    }
}
