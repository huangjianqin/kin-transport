package org.kin.transport.netty.http.server;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author huangjianqin
 * @date 2022/11/9
 */
final class HttpRoutesAcceptor implements Consumer<HttpServerRoutes> {
    private static final Logger log = LoggerFactory.getLogger(HttpRoutesAcceptor.class);
    /**
     * key -> url, value -> 对应的{@link  HttpRequestHandler}
     * 不可变
     */
    private final Map<String, HttpRequestHandler> url2Handler;
    /** http request 拦截器 */
    private final List<HandlerInterceptor> interceptors;

    HttpRoutesAcceptor(Map<String, HttpRequestHandler> url2Handler) {
        //copy, 防止外部使用transport进行修改
        this.url2Handler = new HashMap<>(url2Handler);
        this.interceptors = Collections.emptyList();
    }

    HttpRoutesAcceptor(Map<String, HttpRequestHandler> url2Handler, List<HandlerInterceptor> interceptors) {
        //copy, 防止外部使用transport进行修改
        this.url2Handler = new HashMap<>(url2Handler);
        this.interceptors = new ArrayList<>(interceptors);
    }

    @Override
    public void accept(HttpServerRoutes httpServerRoutes) {
        for (Map.Entry<String, HttpRequestHandler> entry : url2Handler.entrySet()) {
            String url = entry.getKey();
            HttpRequestHandler handler = entry.getValue();
            BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> globalHandler = (req, resp) -> createGlobalHandler(req, resp, handler);
            List<RequestMethod> methods = handler.methods();

            log.info("find mapped [url={}, methods={}] onto {}", url, methods, handler);
            //支持url模糊匹配, 以及/a/{param} path参数匹配
            for (RequestMethod requestMethod : methods) {
                switch (requestMethod) {
                    case PUT:
                        httpServerRoutes.put(url, globalHandler);
                        break;
                    case POST:
                        httpServerRoutes.post(url, globalHandler);
                        break;
                    case DELETE:
                        httpServerRoutes.delete(url, globalHandler);
                        break;
                    case GET:
                        httpServerRoutes.get(url, globalHandler);
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("does not support to handle http method '%s' now", requestMethod));
                }
            }
        }
    }

    private Publisher<Void> createGlobalHandler(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        Publisher<Void> publisher = null;
        for (HandlerInterceptor interceptor : interceptors) {
            publisher = interceptor.preHandle(request, response, handler);
            if (Objects.nonNull(publisher)) {
                break;
            }
        }

        Exception exception = null;
        if (Objects.isNull(publisher)) {
            //pre handle没有拦截请求
            try {
                publisher = handler.doRequest(request, response);
            } catch (Exception e) {
                exception = e;
            }
            if (Objects.isNull(exception)) {
                //request handler执行没有异常才执行post handle
                for (HandlerInterceptor interceptor : interceptors) {
                    interceptor.postHandle(request, response, handler);
                }
            }
        }

        if (Objects.isNull(publisher)) {
            //request handler不返回值
            publisher = Mono.empty();
        }

        if (publisher instanceof NettyOutbound) {
            //如果interceptor返回response send的返回值, 那么需要做一次转换
            //也算是兜底, 防止send之后没有then, 那么就直接返回NettyOutbound了
            publisher = ((NettyOutbound) publisher).then();
        }

        Exception finalException = exception;
        if (publisher instanceof Flux) {
            return ((Flux<Void>) publisher).doOnTerminate(() -> afterCompletion(request, response, handler, finalException));
        } else {
            return ((Mono<Void>) publisher).doOnTerminate(() -> afterCompletion(request, response, handler, finalException));
        }
    }

    private void afterCompletion(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler, @Nullable Exception e) {
        for (HandlerInterceptor interceptor : interceptors) {
            interceptor.afterCompletion(request, response, handler, e);
        }
    }
}
