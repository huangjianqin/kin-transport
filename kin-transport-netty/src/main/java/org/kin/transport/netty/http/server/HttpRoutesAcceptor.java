package org.kin.transport.netty.http.server;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.JSON;
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
        return Mono.just(1)
                //切换到业务线程处理
                .publishOn(scheduler)
                .flatMap(v -> createGlobalHandler0(request, response, handler));
    }

    private Mono<Void> createGlobalHandler0(HttpServerRequest request, HttpServerResponse response, HttpRequestHandler handler) {
        Publisher<Void> resultPublisher = null;
        for (HandlerInterceptor interceptor : interceptors) {
            resultPublisher = interceptor.preHandle(request, response, handler);
            if (Objects.nonNull(resultPublisher)) {
                break;
            }
        }

        //是否pre handle拦截请求
        boolean latchOrNot = Objects.nonNull(resultPublisher);
        if (!latchOrNot) {
            //pre handle没有拦截
            //本质上返回Mono<Object> or Mono<List>
            resultPublisher = handler
                    .doRequest(request, response)
                    //异常, 则将reactive stream元素切换为异常实例
                    .onErrorResume(Mono::just)
                    .flatMap(obj -> {
                        Throwable throwable;
                        if (!(obj instanceof Throwable)) {
                            //正常返回
                            try {
                                for (HandlerInterceptor interceptor : interceptors) {
                                    interceptor.postHandle(request, response, handler);
                                }

                                return response.sendString(Mono.just(obj instanceof String ? obj.toString() : JSON.write(obj)), StandardCharsets.UTF_8).then();
                            } catch (Exception e) {
                                throwable = e;
                            }
                        } else {
                            throwable = (Throwable) obj;
                        }

                        //异常处理
                        ExceptionHandler<Throwable> exceptionHandler = getExceptionHandler(throwable);
                        response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                        Mono<String> errorMessage = null;
                        if (Objects.nonNull(exceptionHandler)) {
                            try {
                                errorMessage = exceptionHandler.onException(request, throwable);
                            } catch (Exception e) {
                                //do nothing
                            }
                        }

                        if (Objects.isNull(errorMessage)) {
                            errorMessage = Mono.just("server encounter fatal error!!!");
                        }

                        return response.sendString(errorMessage, StandardCharsets.UTF_8).then();
                    });
        }

        //preHandle or handler结果转换
        Mono<Void> resultMono;
        if (resultPublisher instanceof NettyOutbound) {
            //如果返回response send的返回值, 那么需要做一次转换
            //也算是兜底, 防止send之后没有then, 那么就直接返回NettyOutbound了
            resultMono = ((NettyOutbound) resultPublisher).then();
        } else {
            if (resultPublisher instanceof Flux) {
                resultMono = ((Flux<Void>) resultPublisher).then();
            } else {
                resultMono = (Mono<Void>) resultPublisher;
            }
        }

        //切换到业务线程处理
        resultMono = resultMono.publishOn(scheduler);

        return resultMono
                //将业务线程绑定上reactive stream context
                .contextWrite(context -> context.put(Scheduler.class, scheduler))
                .doOnTerminate(() -> {
                    for (HandlerInterceptor interceptor : interceptors) {
                        interceptor.afterCompletion(request, response, handler);
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
