package org.kin.transport.netty.http.server;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import javax.annotation.Nullable;

/**
 * http handler拦截器
 *
 * @author huangjianqin
 * @date 2022/11/10
 * @see HttpRequestHandler
 */
public interface HandlerInterceptor {
    /**
     * http request handler执行前逻辑处理
     * @param request   http request
     * @param response  http response
     * @param handler   http request handler
     * @return null表示可以交给下一个interceptor / http request handler处理, 否则直接返回, 结束http request.
     * 注意, 返回值必须要是{@link HttpServerResponse#send()}, {@link HttpServerResponse#send(Publisher)}等response send方法返回值
     */
    default Publisher<Void> preHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        return null;
    }

    /**
     * http request handler执行后逻辑处理
     * @param request   http request
     * @param response  http response
     * @param handler   http request handler
     */
    default void postHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
    }

    /**
     * http request处理完成后逻辑处理
     * @param request   http request
     * @param response  http response
     * @param handler   http request handler
     * @param e         http request hanlder处理过程中遇到的异常
     */
    default void afterCompletion(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler, @Nullable Exception e) {
    }
}
