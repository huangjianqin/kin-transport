package org.kin.transport.netty.http.server;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.CollectionUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
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
    /** http request处理线程池, 减少对netty io的影响 */
    private final Scheduler scheduler;
    /** 已注册的异常handler, 按注册顺序匹配 */
    private final List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> exceptionHandlers;

    HttpRoutesAcceptor(Map<String, HttpRequestHandler> url2Handler, List<HandlerInterceptor> interceptors,
                       List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> exceptionHandlers,
                       int threadCap, int queueCap) {
        Preconditions.checkArgument(threadCap > 0, "threadCap must be greater than 0");
        Preconditions.checkArgument(queueCap > 0, "queueCap must be greater than 0");

        //copy, 防止外部使用transport进行修改
        this.url2Handler = new HashMap<>(url2Handler);
        if (CollectionUtils.isNonEmpty(interceptors)) {
            this.interceptors = new ArrayList<>(interceptors);
        } else {
            this.interceptors = Collections.emptyList();
        }
        this.exceptionHandlers = new ArrayList<>(exceptionHandlers);
        this.scheduler = Schedulers.newBoundedElastic(threadCap, queueCap, "kin-http-server-bs", 300);

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
        return Mono.just(1).publishOn(scheduler).flatMap(v -> createGlobalHandler0(request, response, handler));
    }

    private Mono<Void> createGlobalHandler0(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        Publisher<Void> handleResult = null;
        for (HandlerInterceptor interceptor : interceptors) {
            handleResult = interceptor.preHandle(request, response, handler);
            if (Objects.nonNull(handleResult)) {
                break;
            }
        }

        //是否pre handle拦截请求
        boolean latchOrNot = Objects.nonNull(handleResult);
        if (!latchOrNot) {
            //pre handle没有拦截
            handleResult = handler.doRequest(request, response);
        }

        //preHandle or handler结果转换
        Mono<Void> real;
        if (handleResult instanceof NettyOutbound) {
            //如果返回response send的返回值, 那么需要做一次转换
            //也算是兜底, 防止send之后没有then, 那么就直接返回NettyOutbound了
            real = ((NettyOutbound) handleResult).then();
        } else {
            if (handleResult instanceof Flux) {
                real = ((Flux<Void>) handleResult).then();
            } else {
                real = (Mono<Void>) handleResult;
            }
        }
        //转换到业务线程处理
        real = real.publishOn(scheduler);

        return real.contextWrite(context -> context.put(Scheduler.class, scheduler))
                .doOnError(t -> {
                    ExceptionHandler<Throwable> exceptionHandler = getExceptionHandler(t);
                    response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                    Mono<String> errorMessage;
                    if (Objects.nonNull(exceptionHandler)) {
                        errorMessage = exceptionHandler.onException(request, t);
                    } else {
                        errorMessage = Mono.just("server encounter fatal error!!!");
                    }
                    response.sendString(errorMessage, StandardCharsets.UTF_8).then().subscribe();
                })
                .doOnTerminate(() -> {
                    //handler处理异常就会直接抛出error signal, 不会走到这里
                    for (HandlerInterceptor interceptor : interceptors) {
                        interceptor.postHandle(request, response, handler);
                    }
                });
    }

    /**
     * 根据异常寻找对应的异常处理类
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private ExceptionHandler<Throwable> getExceptionHandler(Throwable throwable) {
        for (Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> tuple : exceptionHandlers) {
            Class<? extends Throwable> exceptionClass = tuple.first();
            if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                return (ExceptionHandler<Throwable>) tuple.second();
            }
        }

        return null;
    }
}
