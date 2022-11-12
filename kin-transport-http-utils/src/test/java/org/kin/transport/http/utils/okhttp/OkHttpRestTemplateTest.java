package org.kin.transport.http.utils.okhttp;

import org.kin.transport.http.HttpCallback;
import org.kin.transport.http.HttpHeaders;
import org.kin.transport.http.HttpResponse;

/**
 * @author huangjianqin
 * @date 2019-09-24
 */
public class OkHttpRestTemplateTest {
    public static void main(String[] args) {
        OkHttpRestTemplate restTemplate = new OkHttpRestTemplate();
        System.out.println("-------------------------------------sync----------------------------------------------------");
        System.out.println(restTemplate.get("http://www.baidu.com"));
        System.out.println("-------------------------------------async----------------------------------------------------");
        restTemplate.get("http://www.baidu.com", new HttpCallback<String>() {
            @Override
            public void onReceived(HttpResponse response) {
                String data = response.getData();
                System.out.println(data);
                HttpHeaders headers = response.getHeaders();
                System.out.println(headers);
            }

            @Override
            public void onFailure(Throwable exception) {
                System.err.println(exception);
            }
        });
        restTemplate.close();
    }
}
