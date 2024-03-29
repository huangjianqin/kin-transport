package org.kin.transport.netty.http.server;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.util.NetUtil;
import org.kin.framework.collection.Tuple;
import org.kin.framework.proxy.MethodDefinition;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.proxy.Proxys;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.StringUtils;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.ServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
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
public final class HttpServerTransport extends ServerTransport<HttpServerTransport> {
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
        return bind(NetUtil.LOCALHOST.getHostAddress());
    }

    /**
     * http server绑定端口
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind(int port) {
        return bind(NetUtil.LOCALHOST.getHostAddress(), port);
    }

    /**
     * http server绑定端口
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind2(int port) {
        return bind2(NetUtil.LOCALHOST.getHostAddress(), port);
    }

    /**
     * http server绑定经典端口
     *
     * @param host host name
     */
    public org.kin.transport.netty.http.server.HttpServer bind(String host) {
        return bind(host, 8080);
    }

    /**
     * http server绑定端口
     *
     * @param host host name
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind(String host,
                                                               int port) {
        return bind(host, port, HttpProtocol.HTTP11);
    }

    /**
     * http server绑定端口
     *
     * @param host host name
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind2(String host,
                                                                int port) {
        return bind(host, port, HttpProtocol.H2);
    }

    /**
     * http server绑定端口
     * 全异步
     *
     * @param port server端口
     */
    public org.kin.transport.netty.http.server.HttpServer bind(String host,
                                                               int port,
                                                               HttpProtocol protocol) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "http server host must be not blank");
        Preconditions.checkArgument(port > 0, "http server port must be greater than 0");
        checkRequire();

        reactor.netty.http.server.HttpServer nettyHttpServer = HttpServer.create();

        //要覆盖nettyHttpServer, 其方法返回的不是this, 是新实例
        if (isSsl()) {
            nettyHttpServer = nettyHttpServer.secure(this::serverSsl);
            if (HttpProtocol.H2C.equals(protocol)) {
                //开启ssl, 但使用http2c协议, 升级为http2
                protocol = HttpProtocol.H2;
            }
        } else {
            if (HttpProtocol.H2.equals(protocol)) {
                //没有开启ssl, 但使用http2协议, 降级为http2c
                protocol = HttpProtocol.H2C;
            }
        }

        LoopResources loopResources = LoopResources.create("kin-http-server-" + port, 2, SysUtils.CPU_NUM * 2, false);
        Scheduler bsScheduler = null;
        if (threadCap > 0) {
            bsScheduler = Schedulers.newBoundedElastic(threadCap, queueCap, "kin-http-server-bs-" + port, 300);
        }
        nettyHttpServer = nettyHttpServer
                .host(host)
                .port(port)
                .protocol(protocol)
                .route(new HttpRoutesAcceptor(url2Handler, interceptors, exceptionHandlers, bsScheduler))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //打印底层event和二进制内容
//                .wiretap(false)
                .accessLog(true)
                //>=256KB+client允许接受压缩就开启压缩
                .compress(true)
                .compress(256 * 1024)
                //1min空闲超时
                .idleTimeout(Duration.ofMinutes(1))
                //最多最存在256个待处理的http request
                .maxKeepAliveRequests(256)
                //自定义event loop
                .runOn(loopResources);

        nettyHttpServer = applyOptions(nettyHttpServer);
        nettyHttpServer = applyChildOptions(nettyHttpServer);

        Scheduler finalBsScheduler = bsScheduler;

        Mono<DisposableServer> disposableMono =
                nettyHttpServer
                        .doOnUnbound(d -> {
                            d.onDispose(loopResources);
                            d.onDispose(() -> {
                                if (Objects.nonNull(finalBsScheduler)) {
                                    finalBsScheduler.dispose();
                                }
                            });
                            d.onDispose(() -> log.info("http server(port:{}) closed", port));
                        })
                        .bind()
                        .doOnSuccess(d -> log.info("http server stated on (port):" + port))
                        .doOnError(t -> log.error("http server encounter error when starting", t))
                        .cast(DisposableServer.class);

        return new org.kin.transport.netty.http.server.HttpServer(disposableMono);
    }
}
