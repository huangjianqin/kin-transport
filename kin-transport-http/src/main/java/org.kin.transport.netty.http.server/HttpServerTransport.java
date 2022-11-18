package org.kin.transport.netty.http.server;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.kin.framework.collection.Tuple;
import org.kin.framework.proxy.MethodDefinition;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.proxy.Proxys;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.ServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.LoopResources;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.*;

/**
 * http server启动入口
 *
 * @author huangjianqin
 * @date 2022/11/9
 */
public final class HttpServerTransport extends ServerTransport {
    private static final Logger log = LoggerFactory.getLogger(HttpServerTransport.class);

    /** key -> url, value -> 对应的{@link  HttpRequestHandler} */
    private final Map<String, HttpRequestHandler> url2Handler = new HashMap<>();
    /** http request interceptors */
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    /** 业务线程数, 默认=0, 即使用reactor-http-nio线程处理http请求 */
    private int threadCap;
    /** 最大等待处理任务数 */
    private int queueCap = Integer.MAX_VALUE;
    /** 存储已注册的异常及其handler */
    private final Map<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>> exceptionClass2Handler = new HashMap<>();
    /** 异常handler */
    private final List<Tuple<Class<? extends Throwable>, ExceptionHandler<? extends Throwable>>> exceptionHandlers = new ArrayList<>();

    public static HttpServerTransport create() {
        return new HttpServerTransport();
    }

    private HttpServerTransport() {
    }

    /**
     * 根据controller实例, 解析出http method, 然后注册映射
     *
     * @see Controller
     * @see RequestMapping
     * @see GetMapping
     * @see PostMapping
     * @see DeleteMapping
     * @see PutMapping
     */
    public HttpServerTransport mapping(Object controllerInst) {
        Preconditions.checkNotNull(controllerInst);

        Class<?> controllerClass = controllerInst.getClass();
        if (!controllerClass.isAnnotationPresent(Controller.class)) {
            throw new IllegalArgumentException("target instance doesn't annotate with org.kin.transport.netty.http.server.Controller");
        }

        //url前缀
        String baseUrl = null;
        RequestMapping classRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (Objects.nonNull(classRequestMapping)) {
            baseUrl = classRequestMapping.value();
        }
        if (Objects.isNull(baseUrl)) {
            baseUrl = "";
        }
        for (Method method : controllerClass.getMethods()) {
            if (Modifier.isFinal(method.getModifiers())) {
                //跳过final
                continue;
            }

            if (!isMappingAnnotationPresent(method)) {
                //忽略没有注解的方法
                continue;
            }

            ProxyInvoker<Object> invoker = Proxys.byteBuddy().enhanceMethod(new MethodDefinition<>(controllerInst, method));

            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (Objects.nonNull(getMapping)) {
                mapping0(baseUrl.concat(getMapping.value()), new HttpRequestHandler(invoker, method, Arrays.asList(RequestMethod.GET)));
            }

            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (Objects.nonNull(postMapping)) {
                mapping0(baseUrl.concat(getMapping.value()), new HttpRequestHandler(invoker, method, Arrays.asList(RequestMethod.POST)));
            }

            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            if (Objects.nonNull(deleteMapping)) {
                mapping0(baseUrl.concat(getMapping.value()), new HttpRequestHandler(invoker, method, Arrays.asList(RequestMethod.DELETE)));
            }

            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            if (Objects.nonNull(putMapping)) {
                mapping0(baseUrl.concat(getMapping.value()), new HttpRequestHandler(invoker, method, Arrays.asList(RequestMethod.PUT)));
            }

            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (Objects.nonNull(requestMapping)) {
                RequestMethod[] requestMethods = requestMapping.method();
                if (CollectionUtils.isNonEmpty(requestMethods)) {
                    mapping0(baseUrl.concat(getMapping.value()), new HttpRequestHandler(invoker, method, Arrays.asList(requestMethods)));
                }
            }
        }

        return this;
    }

    /**
     * 映射url和http request handler
     */
    private void mapping0(String url, HttpRequestHandler handler) {
        if (!url2Handler.containsKey(url)) {
            url2Handler.put(url, handler);
        } else {
            throw new IllegalArgumentException(String.format("url '%s' has been mapped, conflict!!", url));
        }
    }

    /**
     * @return 是否有mapping注解的方法
     * @see RequestMapping
     * @see GetMapping
     * @see PostMapping
     * @see DeleteMapping
     * @see PutMapping
     */
    private boolean isMappingAnnotationPresent(Method method) {
        return method.isAnnotationPresent(RequestMapping.class) ||
                method.isAnnotationPresent(GetMapping.class) ||
                method.isAnnotationPresent(PostMapping.class) ||
                method.isAnnotationPresent(PutMapping.class) ||
                method.isAnnotationPresent(DeleteMapping.class);
    }

    /**
     * 添加自定义http request interceptor
     */
    public HttpServerTransport interceptor(HandlerInterceptor interceptor) {
        Preconditions.checkNotNull(interceptor);
        interceptors.add(interceptor);
        return this;
    }

    /**
     * 业务线程数
     */
    public HttpServerTransport threadCap() {
        return threadCap(SysUtils.CPU_NUM * 10);
    }

    /**
     * 业务线程数
     */
    public HttpServerTransport threadCap(int threadCap) {
        Preconditions.checkArgument(threadCap > 0, "threadCap must be greater than 0");
        this.threadCap = threadCap;
        return this;
    }

    /**
     * 最大等待处理任务数
     */
    public HttpServerTransport queueCap(int queueCap) {
        Preconditions.checkArgument(queueCap > 0, "queueCap must be greater than 0");
        this.queueCap = queueCap;
        return this;
    }

    /**
     * 注册异常handler
     */
    public HttpServerTransport doOnException(Class<? extends Throwable> exceptionClass, ExceptionHandler<? extends Throwable> exceptionHandler) {
        Preconditions.checkNotNull(exceptionClass);
        Preconditions.checkNotNull(exceptionHandler);

        ExceptionHandler<? extends Throwable> registered = exceptionClass2Handler.get(exceptionClass);
        if (Objects.isNull(registered)) {
            exceptionClass2Handler.put(exceptionClass, exceptionHandler);
            exceptionHandlers.add(new Tuple<>(exceptionClass, exceptionHandler));
            return this;
        } else {
            throw new IllegalArgumentException(String.format("'%s' has registered to handle exception '%s'", exceptionClass.getName(), registered.getClass().getName()));
        }
    }

    /**
     * http server绑定经典端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind() {
        return bind(8080);
    }

    /**
     * http server绑定端口
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind(int port) {
        return bind(port, HttpProtocol.HTTP11);
    }

    /**
     * http server绑定端口
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind2(int port) {
        return bind(port, HttpProtocol.H2);
    }

    /**
     * http server绑定端口
     * 全异步
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind(int port, HttpProtocol protocol) {
        reactor.netty.http.server.HttpServer nettyHttpServer = HttpServer.create();

        //要覆盖nettyHttpServer, 其方法返回的不是this, 是新实例
        if (isSsl()) {
            nettyHttpServer = nettyHttpServer.secure(this::secure);
        }

        LoopResources loopResources = LoopResources.create("kin-http-server", 2, SysUtils.CPU_NUM * 2, false);
        nettyHttpServer = nettyHttpServer.port(port)
                .protocol(protocol)
                .route(new HttpRoutesAcceptor(url2Handler, interceptors, exceptionHandlers, threadCap, queueCap))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .wiretap(true)
                .accessLog(true)
                //>=256KB+client允许接受压缩就开启压缩
                .compress(true)
                .compress(256 * 1024)
                //5min空闲超时
                .idleTimeout(Duration.ofMinutes(5))
                //最多最存在512个待处理的http request
                .maxKeepAliveRequests(512)
                //自定义event loop
                .runOn(loopResources);

        customServerTransport(nettyHttpServer);

        Mono<DisposableServer> disposableMono =
                nettyHttpServer.bind()
                        .doOnSuccess(d -> log.info("http server stated on (port):" + port))
                        .doOnError(t -> log.error("http server encounter error when starting", t))
                        .cast(DisposableServer.class);
        return new org.kin.transport.netty.http.server.HttpServer(disposableMono.doOnNext(d -> {
            d.onDispose(loopResources);
            d.onDispose(() -> log.info("http server(port:{}) closed", port));
        }));
    }
}
