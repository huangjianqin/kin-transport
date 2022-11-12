package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.utils.JSON;
import org.kin.framework.utils.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 默认http 请求handler
 * 支持自动从request取参数
 *
 * @author huangjianqin
 * @date 2022/11/9
 */
public final class HttpRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);
    /** http请求处理目标方法的调用代理类 */
    private final ProxyInvoker<Object> invoker;
    /** http请求处理目标方法 */
    private final Method method;
    /** http request handler方法参数转换逻辑 */
    private final List<MethodParamConvertor> convertors;
    /** 处理的http methods */
    private final List<RequestMethod> requestMethods;
    /** 是否允许跨域 */
    private final boolean allowCors;

    HttpRequestHandler(ProxyInvoker<Object> invoker, Method method, List<RequestMethod> requestMethods) {
        this.invoker = invoker;
        this.method = method;
        this.convertors = parseMethodParams(method);
        this.requestMethods = requestMethods;
        this.allowCors = method.getDeclaringClass().isAnnotationPresent(AllowCors.class) || method.isAnnotationPresent(AllowCors.class);
    }

    /**
     * 解析方法参数注解
     *
     * @see RequestParam
     * @see RequestBody
     * @see RequestHeader
     */
    private List<MethodParamConvertor> parseMethodParams(Method method) {
        Parameter[] parameters = method.getParameters();
        List<MethodParamConvertor> convertors = new ArrayList<>(parameters.length);

        for (Parameter parameter : parameters) {
            MethodParamConvertor convertor = NoneConvertor.INSTANCE;
            Class<?> type = parameter.getType();
            if (HttpServerRequest.class.equals(type) || HttpServerResponse.class.equals(type) || HttpHeaders.class.equals(type)) {
                convertor = new NoneConvertor(type);
            } else {
                //HttpServerRequest, HttpServerResponse, HttpHeaders 3种类型特殊处理
                for (Annotation annotation : parameter.getAnnotations()) {
                    //匹配到就退出循环, 以防三个注解同时使用到同一参数
                    if (annotation instanceof RequestParam) {
                        convertor = new RequestParamConvertor(type, (RequestParam) annotation);
                        break;
                    } else if (annotation instanceof RequestBody) {
                        convertor = new RequestBodyConvertor(type, (RequestBody) annotation);
                        break;
                    } else if (annotation instanceof RequestHeader) {
                        convertor = new RequestHeaderConvertor(type, (RequestHeader) annotation);
                        break;
                    }
                }
            }

            convertors.add(convertor);
        }

        return Collections.unmodifiableList(convertors);
    }

    /**
     * http 请求处理逻辑
     *
     * @param request  {@link HttpServerRequest}
     * @param response {@link HttpServerResponse}
     * @return signal
     */
    Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response) {
        //目前市面上大多数都是用get post put delete等http method, 所以把request body当成json 处理
        return Mono.deferContextual(context -> {
            Scheduler scheduler = context.get(Scheduler.class);

            return request.receive().asString(StandardCharsets.UTF_8).switchIfEmpty(Mono.just("")).publishOn(scheduler).flatMap(body -> {
                Map<String, String> params = new HashMap<>(4);
                // /test/{param1}/{param2}
                Map<String, String> uirParams = request.params();
                if (Objects.nonNull(uirParams)) {
                    params.putAll(uirParams);
                }
                params.putAll(getQueryParams(request.uri()));
                try {
                    Object ret = invoker.invoke(fillParams(request, response, params, body));
                    if (allowCors) {
                        //允许跨域
                        response.addHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                        response.addHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "*");
                        response.addHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                    }

                    //返回值不是Flux或者Mono, 统一转换成Mono
                    Publisher<?> retPublisher;
                    if (Objects.isNull(ret)) {
                        retPublisher = Mono.empty();
                    } else if (ret instanceof Flux || ret instanceof Mono) {
                        retPublisher = (Publisher<?>) ret;
                    } else {
                        retPublisher = Mono.just(ret);
                    }

                    if (retPublisher instanceof Flux) {
                        return response.sendString(((Flux<?>) retPublisher).map(JSON::write), StandardCharsets.UTF_8);
                    } else {
                        return response.sendString(((Mono<?>) retPublisher).map(JSON::write), StandardCharsets.UTF_8);
                    }
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }).then();
        });
    }

    /**
     * @return accepted http request method
     * @see RequestMethod
     */
    public List<RequestMethod> methods() {
        return requestMethods;
    }

    /**
     * 从request uri解析出参数map
     */
    private Map<String, String> getQueryParams(String uri) {
        //默认先从url获取参数
        Map<String, String> params = new HashMap<>(4);
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
            //entry.getValue()是一个List, 只取第一个元素
            params.put(entry.getKey(), entry.getValue().get(0));
        }

        return params;
    }

    /**
     * 根据request handler方法参数取值定义, 填充方法参数
     */
    private Object[] fillParams(HttpServerRequest request, HttpServerResponse response,
                                Map<String, String> params, String body) {
        Object[] handlerParams = new Object[convertors.size()];

        for (int i = 0; i < convertors.size(); i++) {
            MethodParamConvertor convertor = convertors.get(i);
            Class<?> type = convertor.type;
            if (HttpServerRequest.class.equals(type)) {
                handlerParams[i] = request;
            } else if (HttpServerResponse.class.equals(type)) {
                handlerParams[i] = response;
            } else if (HttpHeaders.class.equals(type)) {
                handlerParams[i] = request.requestHeaders();
            } else {
                handlerParams[i] = convertor.convert(request, params, body, request.requestHeaders());
            }
        }

        return handlerParams;
    }

    //getter
    public Object getTarget() {
        return invoker.getProxyObj();
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method.getDeclaringClass() + " -> " + method;
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * request handler方法参数取值定义
     */
    private static abstract class MethodParamConvertor {
        /** 参数类型 */
        protected final Class<?> type;
        /** 是否必须存在, 如果检查到不存在, 则报错 */
        protected final boolean require;

        MethodParamConvertor(Class<?> type, boolean require) {
            this.type = type;
            this.require = require;
        }

        /**
         * 从http请求query, body or header中取值
         *
         * @return http request handler 方法参数真实值
         */
        abstract Object convert(HttpServerRequest request, Map<String, String> queryParams, String body, HttpHeaders headers);

        /**
         * 从json转换真实对象
         *
         * @return 真实对象
         */
        Object json2Obj(String json) {
            if (!String.class.equals(type)) {
                return JSON.read(json, type);
            }
            return json;
        }
    }

    /**
     * 直接赋值为null, 比如没有使用任何注解的参数
     * 有些特殊类型, 如{@link HttpServerRequest}, 需要指定类型
     */
    private static class NoneConvertor extends MethodParamConvertor {
        private static final NoneConvertor INSTANCE = new NoneConvertor(null);

        NoneConvertor(Class<?> type) {
            super(type, false);
        }

        @Override
        Object convert(HttpServerRequest request, Map<String, String> queryParams, String body, HttpHeaders headers) {
            return null;
        }
    }

    /**
     * @see RequestParam
     */
    private static class RequestParamConvertor extends MethodParamConvertor {
        /** uri path query name */
        private final String queryName;
        /** 不存在时, 参数默认值 */
        private final String defaultValue;

        RequestParamConvertor(Class<?> type, RequestParam requestParam) {
            super(type, requestParam.require());
            this.queryName = requestParam.value();
            this.defaultValue = requestParam.defaultValue();
        }

        @Override
        Object convert(HttpServerRequest request, Map<String, String> queryParams, String body, HttpHeaders headers) {
            String param = queryParams.get(queryName);
            if (Objects.isNull(param)) {
                if (require && ParamConstants.DEFAULT_NONE.equals(defaultValue)) {
                    //require = true && 没有设置默认值
                    throw new RequestParamNotFoundException(request, queryName);
                } else {
                    param = defaultValue;
                }
            }

            return json2Obj(param);
        }
    }

    /**
     * @see RequestBody
     */
    private static class RequestBodyConvertor extends MethodParamConvertor {
        RequestBodyConvertor(Class<?> type, RequestBody requestBody) {
            super(type, requestBody.require());
        }

        @Override
        Object convert(HttpServerRequest request, Map<String, String> queryParams, String body, HttpHeaders headers) {
            if (StringUtils.isBlank(body)) {
                if (require) {
                    throw new RequestBodyNotFoundException(request);
                }
                return null;
            } else {
                //非空
                return json2Obj(body);
            }
        }
    }

    /**
     * @see RequestHeader
     */
    private class RequestHeaderConvertor extends MethodParamConvertor {
        /** http request header name */
        private final String header;
        /** 不存在时, 参数默认值 */
        private final String defaultValue;

        RequestHeaderConvertor(Class<?> type, RequestHeader requestHeader) {
            super(type, requestHeader.require());
            this.header = requestHeader.value();
            this.defaultValue = requestHeader.defaultValue();
        }

        @Override
        Object convert(HttpServerRequest request, Map<String, String> queryParams, String body, HttpHeaders headers) {
            String v = headers.get(header);
            if (StringUtils.isBlank(v)) {
                if (require && ParamConstants.DEFAULT_NONE.equals(defaultValue)) {
                    //require = true && 没有设置默认值
                    throw new RequestHeaderNotFoundException(request, header);
                } else {
                    v = defaultValue;
                }
            }
            return json2Obj(v);
        }
    }
}
