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
    @Nullable
    default Publisher<Void> preHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        return null;
    }

    /**
     * http request handler执行后逻辑处理
     * <p>
     * 注意: response仅仅支持send一次, 如果在postHandle方法中调用send相关方法并且subscribe, 那么该response就会执行响应,
     * 结果是导致{@link HttpRoutesAcceptor}的global handler中postHandle之后定义的逻辑都无法执行
     *
     * @param request  http request
     * @param response http response
     * @param handler  http request handler
     */
    default void postHandle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
    }
}
