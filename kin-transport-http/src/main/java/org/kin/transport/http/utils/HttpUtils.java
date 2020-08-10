package org.kin.transport.http.utils;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.kin.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2019-09-24
 * <p>
 * okhttp优势:
 * api易用
 * 支持多路复用
 * 支持GZIP压缩减少网络流量
 * 连接池化
 * 因此大量请求时性能更佳
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    /**
     * 与原实例共享线程池、连接池和其他设置项，只需进行少量配置就可以实现特殊需求
     * .newBuilder()
     *
     * ----
     * 最好只使用一个共享的OkHttpClient实例，将所有的网络请求都通过这个实例处理。
     * 因为每个OkHttpClient 实例都有自己的连接池和线程池，重用这个实例能降低延时，减少内存消耗，而重复创建新实例则会浪费资源。
     * OkHttpClient的线程池和连接池在空闲的时候会自动释放，所以一般情况下不需要手动关闭，但是如果出现极端内存不足的情况，可以使用以下代码释放内存：
     * client.dispatcher().executorService().shutdown();   //清除并关闭线程池
     * client.connectionPool().evictAll();                 //清除并关闭连接池
     * client.cache().close();                             //清除cache
     */
    private static final OkHttpClient CLIENT =
            new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.SECONDS)
                    .callTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .writeTimeout(3, TimeUnit.SECONDS)
                    .addInterceptor(new LoggingInterceptor())
                    .build();
    private static final okhttp3.MediaType MEDIATYPE_JSON = okhttp3.MediaType.get("application/json; charset=utf-8");
    private static final okhttp3.MediaType MEDIATYPE_MULTIPART_FORM_DATA = okhttp3.MediaType.get("multipart/form-data; charset=ISO_8859_1");
    private static final okhttp3.MediaType MEDIATYPE_APPLICATION_FORM_URLENCODED = okhttp3.MediaType.get("application/x-www-form-urlencoded; charset=ISO_8859_1");

    /**
     * ResponseBody必须关闭，不然可能造成资源泄漏，你可以通过以下方法关闭ResponseBody,对同一个ResponseBody只要关闭一次就可以了。
     * Response.close();
     * Response.body().close();
     * Response.body().source().close();
     * Response.body().charStream().close();
     * Response.body().byteString().close();
     * Response.body().bytes();
     * Response.body().string();
     *
     * ResponseBody只能被消费一次，也就是string(),bytes(),byteStream()或 charStream()方法只能调用其中一个。
     * 如果ResponseBody中的数据很大，则不应该使用bytes() 或 string()方法，它们会将结果一次性读入内存，而应该使用byteStream()或 charStream()，以流的方式读取数据。
     *
     * ---
     * 每个Call对象只能执行一次请求
     * 如果想重复执行相同的请求，可以：
     * client.newCall(call.request());     //获取另一个相同配置的Call对象
     */

    private static class LoggingInterceptor implements Interceptor {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            log.debug("Sending request {} on {}-{}", request.url(), chain.connection(), request.headers());

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            log.debug("Received response for {} in {}ms-{}", response.request().url(), (t2 - t1) / 1e6d, response.headers());

            return response;
        }
    }

    public static abstract class OkHttp3Callback<T> implements Callback {

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            failure(call, e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            T data = null;
            Class<T> respClass = respClass();
            if (response.isSuccessful() &&
                    respClass != null &&
                    !String.class.equals(respClass) &&
                    response.body() != null
            ) {
                data = converter2Obj(response.body().string(), respClass);
            }
            response(call, response, data);
        }

        public Class<T> respClass() {
            return null;
        }

        /**
         * http请求失败
         *
         * @param call http请求信息
         * @param e    异常
         */
        public abstract void failure(@NotNull Call call, @NotNull IOException e);

        /**
         * http请求成功
         * @param call http请求信息
         * @param response http请求返回
         * @param data 返回的数据(json反序列化)
         * @throws IOException 中途可能抛出的异常
         */
        public abstract void response(@NotNull Call call, @NotNull Response response, T data) throws IOException;
    }

    //------------------------------------------------------------api----------------------------------------------------

    public static OkHttpClient getCLIENT() {
        return CLIENT;
    }

    private static <T> T converter2Obj(String respData, Class<T> respClass) {
        if (StringUtils.isNotBlank(respData)) {
            return JSONObject.parseObject(respData, respClass);
        }

        return null;
    }

    //------------------------------------------------------------sync api----------------------------------------------------

    public static <T> T get(String url, Class<T> respClass) {
        return converter2Obj(get(url), respClass);
    }

    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    //-------------------------------------------------------

    public static <T> T post(String url, Class<T> respClass) {
        return converter2Obj(post(url), respClass);
    }

    public static <T> T post(String url, Map<String, Object> params, Class<T> respClass) {
        return converter2Obj(post(url, params, MediaType.JSON), respClass);
    }

    public static String post(String url) {
        return post(url, Collections.emptyMap(), MediaType.JSON);
    }

    public static String post(String url, Map<String, Object> params) {
        return post(url, params, MediaType.JSON);
    }
    //-------------------------------------------------------

    public static <T> T post(String url, Class<T> respClass, MediaType mediaType) {
        return converter2Obj(post(url, mediaType), respClass);
    }

    public static <T> T post(String url, Map<String, Object> params, Class<T> respClass, MediaType mediaType) {
        return converter2Obj(post(url, params, mediaType), respClass);
    }

    public static String post(String url, MediaType mediaType) {
        return post(url, Collections.emptyMap(), mediaType);
    }

    public static String post(String url, Map<String, Object> params, MediaType mediaType) {
        Request request = new Request.Builder()
                .url(url)
                .post(mediaType.createRequestBody(params))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    //------------------------------------------------------------async api----------------------------------------------------

    public static void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        CLIENT.newCall(request).enqueue(callback);
    }

    //-----------------------------------------------

    public static void post(String url, Callback callback) {
        post(url, Collections.emptyMap(), callback);
    }

    public static void post(String url, Map<String, Object> params, Callback callback) {
        post(url, params, MediaType.JSON, callback);
    }
    //-----------------------------------------------

    public static void post(String url, MediaType mediaType, Callback callback) {
        post(url, Collections.emptyMap(), mediaType, callback);
    }

    public static void post(String url, Map<String, Object> params, MediaType mediaType, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(mediaType.createRequestBody(params))
                .build();
        CLIENT.newCall(request).enqueue(callback);
    }

    /**
     *
     */
    public enum MediaType {
        /**
         *
         */
        JSON {
            private String converterMap2JsonStr(Map<String, Object> params) {
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }

                return jsonObject.toJSONString();
            }

            @Override
            public RequestBody createRequestBody(Map<String, Object> params) {
                return RequestBody.create(converterMap2JsonStr(params), MEDIATYPE_JSON);
            }
        },
        /**
         *
         */
        FORM {
            @Override
            public RequestBody createRequestBody(Map<String, Object> params) {
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    formBodyBuilder.add(entry.getKey(), entry.getValue().toString());
                }

                return formBodyBuilder.build();
            }
        };

        /**
         * 根据类型转换成对应的RequestBody
         */
        public abstract RequestBody createRequestBody(Map<String, Object> params);
    }

    public static abstract class AsyncHttpRequestCallback<T> implements Callback {
        @Override
        public final void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            if (response.isSuccessful() && response.body() != null) {
                doResponse(converter2Obj(response.body().string(), getResponseObj()));
            }
        }

        @Override
        public final void onFailure(@NotNull Call call, @NotNull IOException e) {

        }

        /**
         * http请求成功处理
         * @param responseObj 返回的数据(json反序列化)
         */
        public abstract void doResponse(T responseObj);

        /**
         * 获取http返回数据对象
         * @return http返回数据对象
         */
        abstract Class<T> getResponseObj();
    }
}
