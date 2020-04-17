package org.kin.transport.http;

import okhttp3.Call;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.kin.transport.http.utils.HttpUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2019-09-24
 */
public class HttpUtilTest {
    public static void main(String[] args) {
        System.out.println(HttpUtils.get("http://www.baidu.com"));

        Map<String, Object> params1 = new HashMap<>();
        params1.put("keys", Collections.singleton("a"));
        params1.put("appName", "demo");
        params1.put("env", "test");
        System.out.println(HttpUtils.post("http://localhost:8080/conf/find", params1));

        System.out.println("-------------------------------------async----------------------------------------------------");

        HttpUtils.get("http://www.baidu.com", new HttpUtils.OkHttp3Callback<String>() {
            @Override
            public void failure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void response(@NotNull Call call, @NotNull Response response, String data) throws IOException {
                System.out.println(response.body().string());
            }
        });

        Map<String, Object> params2 = new HashMap<>();
        HttpUtils.post("http://localhost:8080/conf/find", params1, new HttpUtils.OkHttp3Callback<ConfFindResult>() {
            @Override
            public Class<ConfFindResult> respClass() {
                return ConfFindResult.class;
            }

            @Override
            public void failure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void response(@NotNull Call call, @NotNull Response response, ConfFindResult data) {
                System.out.println(data);
            }
        });
    }

    private class ConfFindResult {
        private int code;
        private String msg;
        private Map<String, Object> data;

        //setter && getter
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "ConfFindResult{" +
                    "code=" + code +
                    ", msg='" + msg + '\'' +
                    ", data=" + data +
                    '}';
        }
    }
}
