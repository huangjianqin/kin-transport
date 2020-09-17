package org.kin.transport.netty.http;

import org.kin.transport.netty.http.client.HttpRequest;
import org.kin.transport.netty.http.client.HttpResponse;
import org.kin.transport.netty.http.client.KinHttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2020/9/17
 */
public class HttpClientTest {
    private static KinHttpClient kinHttpClient = KinHttpClient.builder();

    public static void main(String[] args) throws InterruptedException {
        get();
        Thread.sleep(2000);
        post();

        Thread.sleep(5000);
    }

    public static void get() {
        try {
            HttpRequest httpRequest = HttpRequest.of("http://127.0.0.1:8880/a").get();
            HttpResponse response = kinHttpClient.newCall(httpRequest).execute();
            System.out.println(response.code());
            System.out.println(response.headers());
            System.out.println(response.message());
            System.out.println(response.responseBody().getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void post() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("index", 1);
            HttpRequest httpRequest = HttpRequest.of("http://127.0.0.1:8880/a").post(MediaType.JSON.toRequestBody(params, "utf-8"));
            HttpResponse response = kinHttpClient.newCall(httpRequest).execute();
            System.out.println(response.code());
            System.out.println(response.headers());
            System.out.println(response.message());
            System.out.println(response.responseBody().getParams());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
