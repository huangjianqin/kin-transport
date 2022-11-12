package org.kin.transport.http.utils.okhttp;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.kin.framework.utils.JSON;
import org.kin.transport.http.HttpCallback;
import org.kin.transport.http.HttpHeaders;
import org.kin.transport.http.HttpResponse;
import org.kin.transport.http.utils.AbstractRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * okhttp优势:
 * api易用
 * 支持多路复用
 * 支持GZIP压缩减少网络流量
 * 连接池化
 * 因此大量请求时性能更佳
 * <p>
 * ResponseBody必须关闭，不然可能造成资源泄漏，你可以通过以下方法关闭ResponseBody,对同一个ResponseBody只要关闭一次就可以了。
 * Response.close();
 * Response.body().close();
 * Response.body().source().close();
 * Response.body().charStream().close();
 * Response.body().byteString().close();
 * Response.body().bytes();
 * Response.body().string();
 * <p>
 * ResponseBody只能被消费一次，也就是string(),bytes(),byteStream()或 charStream()方法只能调用其中一个。
 * 如果ResponseBody中的数据很大，则不应该使用bytes() 或 string()方法，它们会将结果一次性读入内存，而应该使用byteStream()或 charStream()，以流的方式读取数据。
 * -----
 * 每个Call对象只能执行一次请求
 * 如果想重复执行相同的请求，可以：
 * //获取另一个相同配置的Call对象
 * client.newCall(call.request());
 *
 * @author huangjianqin
 * @date 2021/12/31
 */
public final class OkHttpRestTemplate extends AbstractRestTemplate {
    private static final Logger log = LoggerFactory.getLogger(OkHttpRestTemplate.class);
    /** json okhttp media type */
    private static final okhttp3.MediaType MEDIATYPE_JSON = okhttp3.MediaType.get("application/json; charset=utf-8");

    /**
     * 与原实例共享线程池、连接池和其他设置项，只需进行少量配置就可以实现特殊需求
     * .newBuilder()
     * ----
     * 最好只使用一个共享的OkHttpClient实例，将所有的网络请求都通过这个实例处理。
     * 因为每个OkHttpClient 实例都有自己的连接池和线程池，重用这个实例能降低延时，减少内存消耗，而重复创建新实例则会浪费资源。
     * OkHttpClient的线程池和连接池在空闲的时候会自动释放，所以一般情况下不需要手动关闭，但是如果出现极端内存不足的情况，可以使用以下代码释放内存
     */
    private final OkHttpClient client;

    public OkHttpRestTemplate() {
        this(new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build());

    }

    public OkHttpRestTemplate(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 将{@link Response} 转换成{@link HttpResponse}
     */
    private HttpResponse toHttpResponse(Response response, Class<?> respClass) {
        int code = response.code();
        HttpHeaders httpHeaders = new HttpHeaders();
        Headers headers = response.headers();
        httpHeaders.putAll(headers.toMultimap());
        InputStream input = null;
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            input = body.byteStream();
        }
        HttpResponse httpResponse = new HttpResponse(code, httpHeaders);
        httpResponse.setData(convert(httpResponse, input, respClass));
        return httpResponse;
    }

    /**
     * 将{@link HttpHeaders} 转换成{@link Headers}
     */
    private Headers toOkHttpHeaders(HttpHeaders httpHeaders) {
        return Headers.of(httpHeaders.toSingleValueMap());
    }

    /**
     * sync http request统一逻辑
     */
    private <T> T execute(Call call, Class<T> respClass) {
        try (Response response = call.execute()) {
            HttpResponse httpResponse = toHttpResponse(response, respClass);
            return httpResponse.getData();
        } catch (IOException e) {
            log.error("", e);
        }

        return null;
    }

    /**
     * http get统一逻辑
     */
    private Call get0(String url, HttpHeaders headers) {
        Request request = new Request.Builder()
                .url(url)
                .headers(toOkHttpHeaders(headers))
                .build();

        return client.newCall(request);
    }

    /**
     * http post统一逻辑
     */
    private Call post0(String url, HttpHeaders headers, RequestBody requestBody) {
        Request request = new Request.Builder()
                .url(url)
                .headers(toOkHttpHeaders(headers))
                .post(requestBody)
                .build();

        return client.newCall(request);
    }

    /**
     * 基于form的http post统一逻辑
     */
    private Call postForm0(String url, HttpHeaders headers, Map<String, Object> body) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue().toString());
        }

        return post0(url, headers, formBodyBuilder.build());
    }

    @Override
    public <T> T get(String url, HttpHeaders headers, Class<T> respClass) {
        return execute(get0(url, headers), respClass);
    }

    @Override
    public <T> T postJson(String url, HttpHeaders headers, Object body, Class<T> respClass) {
        return execute(postJson0(url, headers, body), respClass);
    }

    private Call postJson0(String url, HttpHeaders headers, Object body) {
        return post0(url, headers, RequestBody.create(JSON.write(body), MEDIATYPE_JSON));
    }

    @Override
    public <T> T postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass) {
        return execute(postForm0(url, headers, body), respClass);
    }

    @Override
    public <T> void get(String url, HttpHeaders headers, Class<T> respClass, HttpCallback<T> callback) {
        get0(url, headers).enqueue(new OkHttp3Callback<>(callback, respClass));
    }

    @Override
    public <T> void postJson(String url, HttpHeaders headers, Object body, Class<T> respClass, HttpCallback<T> callback) {
        postJson0(url, headers, body).enqueue(new OkHttp3Callback<>(callback, respClass));
    }

    @Override
    public <T> void postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass, HttpCallback<T> callback) {
        postForm0(url, headers, body).enqueue(new OkHttp3Callback<>(callback, respClass));
    }

    /**
     * close okhttp client
     */
    public void close() {
        //清除并关闭线程池
        client.dispatcher().executorService().shutdown();
        //清除并关闭连接池
        client.connectionPool().evictAll();
        //清除cache
        try {
            Cache cache = client.cache();
            if (Objects.nonNull(cache)) {
                cache.close();
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------

    /**
     * 输出每次http请求链的log
     */
    public static class LoggingInterceptor implements Interceptor {
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

    /**
     * 内置{@link Callback}实现
     */
    private class OkHttp3Callback<T> implements Callback {
        /** user自定义callback */
        private final HttpCallback<T> callback;
        /** received http message */
        private final Class<T> respClass;

        public OkHttp3Callback(HttpCallback<T> callback, Class<T> respClass) {
            this.callback = callback;
            this.respClass = respClass;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            //异常callback
            callback.onFailure(e);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            try {
                HttpResponse httpResponse = toHttpResponse(response, respClass);
                callback.onReceived(httpResponse);
            } catch (Exception e) {
                //异常callback
                callback.onFailure(e);
            }
        }
    }
}
