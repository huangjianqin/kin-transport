package org.kin.transport.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * restful风格http请求模板
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
public interface RestTemplate {
    /**
     * 填充带参数的url, 比如host:port/{K1}/{K2}
     */
    default String expandUrl(String source, Map<String, Object> uriVariables) {
        for (Map.Entry<String, Object> entry : uriVariables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            source = source.replaceAll("\\{" + key + "\\}", value);
        }

        return source;
    }

    /**
     * 填充带参数的url, 比如host:port/{0}/{1}
     */
    default String expandUrl(String source, Object... uriVariables) {
        return expandUrl(source, Arrays.asList(uriVariables));
    }

    /**
     * 填充带参数的url, 比如host:port/{0}/{1}
     */
    default String expandUrl(String source, Collection<Object> uriVariables) {
        int idx = 0;
        for (Object uriVariable : uriVariables) {
            source = source.replaceAll("\\{" + (idx++) + "\\}", uriVariable.toString());
        }
        return source;
    }

    //----------------------------------------------------------------sync----------------------------------------------------------------

    /**
     * http get
     */
    default String get(String url) {
        return get(url, String.class);
    }

    /**
     * http get
     */
    default <T> T get(String url, Class<T> respClass) {
        return get(url, Collections.emptyMap(), respClass);
    }

    /**
     * http get
     */
    default <T> T get(String url, Map<String, Object> uriVariables, Class<T> respClass) {
        return get(url, HttpHeaders.EMPTY, uriVariables, respClass);
    }

    /**
     * http get
     */
    default <T> T get(String url, Class<T> respClass, Object... uriVariables) {
        return get(url, HttpHeaders.EMPTY, respClass, uriVariables);
    }

    /**
     * http get
     */
    default String get(String url, HttpHeaders headers) {
        return get(url, headers, String.class);
    }

    /**
     * http get
     */
    default <T> T get(String url, HttpHeaders headers, Map<String, Object> uriVariables, Class<T> respClass) {
        return get(expandUrl(url, uriVariables), headers, respClass);
    }

    /**
     * http get
     */
    default <T> T get(String url, HttpHeaders headers, Class<T> respClass, Object... uriVariables) {
        return get(expandUrl(url, uriVariables), headers, respClass);
    }

    /**
     * http get
     */
    <T> T get(String url, HttpHeaders headers, Class<T> respClass);

    /**
     * 基于json的http post
     */
    default String postJson(String url, Object body) {
        return postJson(url, body, String.class);
    }

    /**
     * 基于json的http post
     */
    default <T> T postJson(String url, Object body, Class<T> respClass) {
        return postJson(url, body, Collections.emptyMap(), respClass);
    }

    /**
     * 基于json的http post
     */
    default <T> T postJson(String url, Object body, Map<String, Object> uriVariables, Class<T> respClass) {
        return postJson(url, new HttpHeaders(), body, uriVariables, respClass);
    }

    /**
     * 基于json的http post
     */
    default <T> T postJson(String url, Object body, Class<T> respClass, Object... uriVariables) {
        return postJson(url, new HttpHeaders(), body, respClass, uriVariables);
    }

    /**
     * 基于json的http post
     */
    default String postJson(String url, HttpHeaders headers, Object body) {
        return postJson(url, headers, body, String.class);
    }

    /**
     * 基于json的http post
     */
    default <T> T postJson(String url, HttpHeaders headers, Object body, Map<String, Object> uriVariables, Class<T> respClass) {
        return postJson(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于json的http post
     */
    default <T> T postJson(String url, HttpHeaders headers, Object body, Class<T> respClass, Object... uriVariables) {
        return postJson(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于json的http post
     */
    <T> T postJson(String url, HttpHeaders headers, Object body, Class<T> respClass);

    /**
     * 基于form的http post
     */
    default String postForm(String url, Map<String, Object> body) {
        return postForm(url, body, String.class);
    }

    /**
     * 基于form的http post
     */
    default <T> T postForm(String url, Map<String, Object> body, Class<T> respClass) {
        return postForm(url, body, Collections.emptyMap(), respClass);
    }

    /**
     * 基于form的http post
     */
    default <T> T postForm(String url, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass) {
        return postForm(url, new HttpHeaders(), body, uriVariables, respClass);
    }

    /**
     * 基于form的http post
     */
    default <T> T postForm(String url, Map<String, Object> body, Class<T> respClass, Object... uriVariables) {
        return postForm(url, new HttpHeaders(), body, respClass, uriVariables);
    }

    /**
     * 基于form的http post
     */
    default String postForm(String url, HttpHeaders headers, Map<String, Object> body) {
        return postForm(url, headers, body, String.class);
    }

    /**
     * 基于form的http post
     */
    default <T> T postForm(String url, HttpHeaders headers, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass) {
        return postForm(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于form的http post
     */
    default <T> T postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass, Object... uriVariables) {
        return postForm(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于form的http post
     */
    <T> T postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass);

    //----------------------------------------------------------------async----------------------------------------------------------------

    /**
     * 异步http get
     */
    default void get(String url, HttpCallback<String> callback) {
        get(url, String.class, callback);
    }

    /**
     * 异步http get
     */
    default <T> void get(String url, Class<T> respClass, HttpCallback<T> callback) {
        get(url, Collections.emptyMap(), respClass, callback);
    }

    /**
     * 异步http get
     */
    default <T> void get(String url, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        get(url, HttpHeaders.EMPTY, uriVariables, respClass, callback);
    }

    /**
     * 异步http get
     */
    default <T> void get(String url, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        get(url, HttpHeaders.EMPTY, respClass, callback, uriVariables);
    }

    /**
     * 异步http get
     */
    default void get(String url, HttpHeaders headers, HttpCallback<String> callback) {
        get(url, headers, String.class, callback);
    }

    /**
     * 异步http get
     */
    default <T> void get(String url, HttpHeaders headers, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        get(expandUrl(url, uriVariables), headers, respClass, callback);
    }

    /**
     * 异步http get
     */
    default <T> void get(String url, HttpHeaders headers, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        get(expandUrl(url, uriVariables), headers, respClass, callback);
    }

    /**
     * 异步http get
     */
    <T> void get(String url, HttpHeaders headers, Class<T> respClass, HttpCallback<T> callback);

    /**
     * 基于json的异步http post
     */
    default void postJson(String url, Object body, HttpCallback<String> callback) {
        postJson(url, body, String.class, callback);
    }

    /**
     * 基于json的异步http post
     */
    default <T> void postJson(String url, Object body, Class<T> respClass, HttpCallback<T> callback) {
        postJson(url, body, Collections.emptyMap(), respClass, callback);
    }

    /**
     * 基于json的异步http post
     */
    default <T> void postJson(String url, Object body, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        postJson(url, new HttpHeaders(), body, uriVariables, respClass, callback);
    }

    /**
     * 基于json的异步http post
     */
    default <T> void postJson(String url, Object body, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        postJson(url, new HttpHeaders(), body, respClass, callback, uriVariables);
    }

    /**
     * 基于json的异步http post
     */
    default void postJson(String url, HttpHeaders headers, Object body, HttpCallback<String> callback) {
        postJson(url, headers, body, String.class, callback);
    }

    /**
     * 基于json的异步http post
     */
    default <T> void postJson(String url, HttpHeaders headers, Object body, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        postJson(expandUrl(url, uriVariables), headers, body, respClass, callback);
    }

    /**
     * 基于json的异步http post
     */
    default <T> void postJson(String url, HttpHeaders headers, Object body, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        postJson(expandUrl(url, uriVariables), headers, body, respClass, callback);
    }

    /**
     * 基于json的异步http post
     */
    <T> void postJson(String url, HttpHeaders headers, Object body, Class<T> respClass, HttpCallback<T> callback);

    /**
     * 基于form的异步http post
     */
    default void postForm(String url, Map<String, Object> body, HttpCallback<String> callback) {
        postForm(url, body, String.class, callback);
    }

    /**
     * 基于form的异步http post
     */
    default <T> void postForm(String url, Map<String, Object> body, Class<T> respClass, HttpCallback<T> callback) {
        postForm(url, body, Collections.emptyMap(), respClass, callback);
    }

    /**
     * 基于form的异步http post
     */
    default <T> void postForm(String url, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        postForm(url, new HttpHeaders(), body, uriVariables, respClass, callback);
    }

    /**
     * 基于form的异步http post
     */
    default <T> void postForm(String url, Map<String, Object> body, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        postForm(url, new HttpHeaders(), body, respClass, callback, uriVariables);
    }

    /**
     * 基于form的异步http post
     */
    default void postForm(String url, HttpHeaders headers, Map<String, Object> body, HttpCallback<String> callback) {
        postForm(url, headers, body, String.class, callback);
    }

    /**
     * 基于form的异步http post
     */
    default <T> void postForm(String url, HttpHeaders headers, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass, HttpCallback<T> callback) {
        postForm(expandUrl(url, uriVariables), headers, body, respClass, callback);
    }

    /**
     * 基于form的异步http post
     */
    default <T> void postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass, HttpCallback<T> callback, Object... uriVariables) {
        postForm(expandUrl(url, uriVariables), headers, body, respClass, callback);
    }

    /**
     * 基于form的异步http post
     */
    <T> void postForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass, HttpCallback<T> callback);

    //----------------------------------------------------------------future----------------------------------------------------------------

    /**
     * 异步http get
     */
    default CompletableFuture<HttpResponse> asyncGet(String url) {
        return asyncGet(url, String.class);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, Class<T> respClass) {
        return asyncGet(url, Collections.emptyMap(), respClass);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncGet(url, HttpHeaders.EMPTY, uriVariables, respClass);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, Class<T> respClass, Object... uriVariables) {
        return asyncGet(url, HttpHeaders.EMPTY, respClass, uriVariables);
    }

    /**
     * 异步http get
     */
    default CompletableFuture<HttpResponse> asyncGet(String url, HttpHeaders headers) {
        return asyncGet(url, headers, String.class);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, HttpHeaders headers, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncGet(expandUrl(url, uriVariables), headers, respClass);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, HttpHeaders headers, Class<T> respClass, Object... uriVariables) {
        return asyncGet(expandUrl(url, uriVariables), headers, respClass);
    }

    /**
     * 异步http get
     */
    default <T> CompletableFuture<HttpResponse> asyncGet(String url, HttpHeaders headers, Class<T> respClass) {
        FutureHttpCallback<T> callback = new FutureHttpCallback<>();
        get(url, headers, respClass, callback);
        return callback.getFuture();
    }

    /**
     * 基于json的异步http post
     */
    default CompletableFuture<HttpResponse> asyncPostJson(String url, Object body) {
        return asyncPostJson(url, body, String.class);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, Object body, Class<T> respClass) {
        return asyncPostJson(url, body, Collections.emptyMap(), respClass);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, Object body, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncPostJson(url, new HttpHeaders(), body, uriVariables, respClass);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, Object body, Class<T> respClass, Object... uriVariables) {
        return asyncPostJson(url, new HttpHeaders(), body, respClass, uriVariables);
    }

    /**
     * 基于json的异步http post
     */
    default CompletableFuture<HttpResponse> asyncPostJson(String url, HttpHeaders headers, Object body) {
        return asyncPostJson(url, headers, body, String.class);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, HttpHeaders headers, Object body, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncPostJson(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, HttpHeaders headers, Object body, Class<T> respClass, Object... uriVariables) {
        return asyncPostJson(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于json的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostJson(String url, HttpHeaders headers, Object body, Class<T> respClass) {
        FutureHttpCallback<T> callback = new FutureHttpCallback<>();
        postJson(url, headers, body, respClass, callback);
        return callback.getFuture();
    }

    /**
     * 基于form的异步http post
     */
    default CompletableFuture<HttpResponse> asyncPostForm(String url, Map<String, Object> body) {
        return asyncPostForm(url, body, String.class);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, Map<String, Object> body, Class<T> respClass) {
        return asyncPostForm(url, body, Collections.emptyMap(), respClass);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncPostForm(url, new HttpHeaders(), body, uriVariables, respClass);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, Map<String, Object> body, Class<T> respClass, Object... uriVariables) {
        return asyncPostForm(url, new HttpHeaders(), body, respClass, uriVariables);
    }

    /**
     * 基于form的异步http post
     */
    default CompletableFuture<HttpResponse> asyncPostForm(String url, HttpHeaders headers, Map<String, Object> body) {
        return asyncPostForm(url, headers, body, String.class);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, HttpHeaders headers, Map<String, Object> body, Map<String, Object> uriVariables, Class<T> respClass) {
        return asyncPostForm(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass, Object... uriVariables) {
        return asyncPostForm(expandUrl(url, uriVariables), headers, body, respClass);
    }

    /**
     * 基于form的异步http post
     */
    default <T> CompletableFuture<HttpResponse> asyncPostForm(String url, HttpHeaders headers, Map<String, Object> body, Class<T> respClass) {
        FutureHttpCallback<T> callback = new FutureHttpCallback<>();
        postForm(url, headers, body, respClass, callback);
        return callback.getFuture();
    }
}
