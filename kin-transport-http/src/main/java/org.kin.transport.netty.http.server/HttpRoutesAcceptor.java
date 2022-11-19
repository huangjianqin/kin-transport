package org.kin.transport.netty.http.server;

import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.UriPathResolver;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

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
    /**
     * key -> url, value -> 对应的{@link  UriPathResolver}
     * 不可变
     */
    private final Map<String, UriPathResolver> url2PathResolver;
    /** http request 拦截器 */
    private final List<HandlerInterceptor> interceptors;
    /**
     * http request处理线程池, 减少对netty io的影响
     */
    private final Scheduler scheduler;
    /** 已注册的异常handler, 按注册顺序匹配 */
    private final List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> exceptionHandlers;

    HttpRoutesAcceptor(Map<String, HttpRequestHandler> url2Handler, List<HandlerInterceptor> interceptors,
                       List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> exceptionHandlers,
                       Scheduler scheduler) {

        //copy, 防止外部使用transport进行修改
        this.url2Handler = new HashMap<>(url2Handler);
        Map<String, UriPathResolver> url2PathResolver = new HashMap<>();
        for (String url : url2Handler.keySet()) {
            url2PathResolver.put(url, new UriPathResolver(url));
        }
        this.url2PathResolver = url2PathResolver;
        if (CollectionUtils.isNonEmpty(interceptors)) {
            this.interceptors = new ArrayList<>(interceptors);
        } else {
            this.interceptors = Collections.emptyList();
        }
        this.exceptionHandlers = new ArrayList<>(exceptionHandlers);
        this.scheduler = scheduler;
    }

    @Override
    public void accept(HttpServerRoutes httpServerRoutes) {
        for (Map.Entry<String, HttpRequestHandler> entry : url2Handler.entrySet()) {
            String url = entry.getKey();
            HttpRequestHandler handler = entry.getValue();
            BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> globalHandler = (req, resp) -> handle(req, resp, handler);
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

    /**
     * route handle处理
     *
     * @return complete signal
     */
    private Publisher<Void> handle(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        if (Objects.isNull(scheduler)) {
            return Mono.just(new InterceptorChain(this, request, response, handler))
                    //切换到业务线程池处理
                    .flatMap(InterceptorChain::next);
        } else {
            return Mono.just(new InterceptorChain(this, request, response, handler))
                    //切换到业务线程池处理
                    .publishOn(scheduler)
                    .flatMap(InterceptorChain::next)
                    .contextWrite(context -> context.put(Scheduler.class, scheduler));
        }
    }

    //getter
    public Map<String, HttpRequestHandler> getUrl2Handler() {
        return url2Handler;
    }

    public Map<String, UriPathResolver> getUrl2PathResolver() {
        return url2PathResolver;
    }

    public List<HandlerInterceptor> getInterceptors() {
        return interceptors;
    }

    public List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> getExceptionHandlers() {
        return exceptionHandlers;
    }
}
