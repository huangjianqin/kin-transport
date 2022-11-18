package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.JSON;
import org.kin.framework.utils.UriPathResolver;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link HandlerInterceptor}实例链
 *
 * @author huangjianqin
 * @date 2022/11/18
 */
public final class InterceptorChain {
    /** http router */
    private final HttpRoutesAcceptor acceptor;
    /** http request */
    private final HttpServerRequest request;
    /** http response */
    private final HttpServerResponse response;
    /** http request handler */
    private HttpRequestHandler handler;
    /** 当前执行中的intercept */
    private int curIndex = -1;

    public InterceptorChain(HttpRoutesAcceptor acceptor, HttpServerRequest request,
                            HttpServerResponse response, HttpRequestHandler handler) {
        this.acceptor = acceptor;
        this.request = request;
        this.response = response;
        this.handler = handler;
    }

    /**
     * 走下一{@link HandlerInterceptor}
     *
     * @return complete signal
     */
    public Mono<Void> next() {
        return Mono.defer(() -> {
            List<HandlerInterceptor> interceptors = acceptor.getInterceptors();
            curIndex++;
            if (curIndex < interceptors.size()) {
                return interceptors.get(curIndex).preHandle(this);
            } else {
                return handle(request, response);
            }
        });
    }

    /**
     * {@link HttpRequestHandler}执行逻辑
     *
     * @return complete signal
     */
    @SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
    private Mono<Void> handle(HttpServerRequest request, HttpServerResponse response) {
        Publisher<Void> resultPublisher = handler
                //本质上返回Mono<Object> or Mono<List>
                .doRequest(request, response)
                //异常, 则将reactive stream元素切换为异常实例
                .onErrorResume(Mono::just)
                .flatMap(obj -> {
                    Throwable throwable;
                    if (!(obj instanceof Throwable)) {
                        //正常返回
                        try {
                            return response.sendString(Mono.just(obj instanceof String ? obj.toString() : JSON.write(obj)), StandardCharsets.UTF_8)
                                    .then();
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

        //preHandle or handler结果转换
        Mono<Void> resultMono;
        if (resultPublisher instanceof NettyOutbound) {
            //如果返回response send的返回值, 那么需要做一次转换
            //也算是兜底, 防止send之后没有then, 那么就直接返回NettyOutbound了
            resultMono = ((NettyOutbound) resultPublisher).then();
        } else {
            resultMono = (Mono<Void>) resultPublisher;
        }

        //将业务线程绑定上reactive stream context
        return resultMono;
    }

    /**
     * 根据异常寻找对应的异常处理类
     *
     * @return complete signal
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private ExceptionHandler<Throwable> getExceptionHandler(Throwable throwable) {
        for (Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> tuple : acceptor.getExceptionHandlers()) {
            Class<? extends Throwable> exceptionClass = tuple.first();
            if (exceptionClass.isAssignableFrom(throwable.getClass())) {
                return (ExceptionHandler<Throwable>) tuple.second();
            }
        }

        return null;
    }

    /**
     * 重定向指定{@code uri} handler处理
     * todo 注意: 目前只是重定向到指定{@link  HttpRequestHandler}, request的内容没有做修改, 还是原来的样子
     *
     * @param uri 重定向uri
     * @return complete signal
     */
    public Mono<Void> redirect(String uri) {
        for (Map.Entry<String, UriPathResolver> entry : acceptor.getUrl2PathResolver().entrySet()) {
            String key = entry.getKey();
            UriPathResolver resolver = entry.getValue();

            if (resolver.matches(uri)) {
                handler = acceptor.getUrl2Handler().get(key);
                return next();
            }
        }

        return response.sendNotFound();
    }

    //getter
    public HttpServerRequest getRequest() {
        return request;
    }

    public HttpServerResponse getResponse() {
        return response;
    }

    public HttpRequestHandler getHandler() {
        return handler;
    }
}
