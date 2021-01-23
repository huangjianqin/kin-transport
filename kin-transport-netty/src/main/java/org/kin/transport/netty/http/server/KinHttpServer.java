package org.kin.transport.netty.http.server;

import org.kin.framework.proxy.MethodDefinition;
import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.proxy.Proxys;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.LazyInstantiation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 对外api
 * 应用上下文
 * 管理servlet配置
 * url匹配按配置顺序匹配
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class KinHttpServer {
    private final String appName;
    /** servlet config */
    private final Queue<ServletConfig> servletConfigs = new ConcurrentLinkedQueue<>();
    /** filter config */
    private final Queue<FilterConfig> filterConfigs = new ConcurrentLinkedQueue<>();
    /** session manager */
    private Class<? extends HttpSessionManager> sessionManagerClass = DefaultSessionManager.class;
    /** 服务是否已启动 */
    private volatile boolean isExport;

    private KinHttpServer(String appName) {
        this.appName = appName;
    }

    public HttpSessionManager getSessionManager() {
        return ClassUtils.instance(sessionManagerClass);
    }

    //getter
    public String getAppName() {
        return appName;
    }

    Queue<ServletConfig> getServletConfigs() {
        return servletConfigs;
    }

    Queue<FilterConfig> getFilterConfigs() {
        return filterConfigs;
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static KinHttpServerBuilder builder(String appName) {
        return new KinHttpServerBuilder(appName);
    }

    public static KinHttpServerBuilder builder() {
        return new KinHttpServerBuilder("default");
    }

    public static class KinHttpServerBuilder {
        private final KinHttpServer kinHttpServer;
        /** 已映射的servlet url */
        private final Set<String> mappedServletUrls = new HashSet<>();
        /** 已映射的filter url */
        private final Set<String> mappedFilterUrls = new HashSet<>();

        public KinHttpServerBuilder(String appName) {
            checkState();
            this.kinHttpServer = new KinHttpServer(appName);
        }

        /**
         * 映射servlet class
         */
        public KinHttpServerBuilder mappingServlet(String url, Class<? extends Servlet> servletClass) {
            return mappingServlet(url, servletClass, null);
        }

        /**
         * 映射servlet class
         */
        public KinHttpServerBuilder mappingServlet(String url, Class<? extends Servlet> servletClass, RequestMethod method) {
            checkState();
            if (mappedServletUrls.add(url)) {
                kinHttpServer.servletConfigs.add(new ServletConfig(url, servletClass, method));
            } else {
                throw new IllegalArgumentException(String.format("servlet for '%s' has been mapped", url));
            }
            return this;
        }

        /**
         * 映射filter class
         */
        public KinHttpServerBuilder mappingFilter(String url, Class<? extends Filter> filterClass) {
            checkState();
            if (mappedServletUrls.add(url)) {
                kinHttpServer.filterConfigs.add(new FilterConfig(url, filterClass));
            } else {
                throw new IllegalArgumentException(String.format("filter for '%s' has been mapped", url));
            }
            return this;
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
        public KinHttpServerBuilder mappingServlet(String url, Object controllerInst) {
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

                GetMapping getMapping = method.getAnnotation(GetMapping.class);
                if (Objects.nonNull(getMapping)) {
                    mappingServlet(baseUrl.concat(getMapping.value()), createMappingAnnoServlet(controllerInst, method, RequestMethod.GET));
                }

                PostMapping postMapping = method.getAnnotation(PostMapping.class);
                if (Objects.nonNull(postMapping)) {
                    mappingServlet(baseUrl.concat(postMapping.value()), createMappingAnnoServlet(controllerInst, method, RequestMethod.POST));
                }

                DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
                if (Objects.nonNull(deleteMapping)) {
                    mappingServlet(baseUrl.concat(deleteMapping.value()), createMappingAnnoServlet(controllerInst, method, RequestMethod.DELETE));
                }

                PutMapping putMapping = method.getAnnotation(PutMapping.class);
                if (Objects.nonNull(putMapping)) {
                    mappingServlet(baseUrl.concat(putMapping.value()), createMappingAnnoServlet(controllerInst, method, RequestMethod.PUT));
                }

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                if (Objects.nonNull(requestMapping)) {
                    for (RequestMethod requestMethod : requestMapping.method()) {
                        mappingServlet(baseUrl.concat(requestMapping.value()), createMappingAnnoServlet(controllerInst, method, requestMethod));
                    }
                }

            }

            return this;
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
         * 根据mapping注解信息创建对应的servlet
         */
        private Servlet createMappingAnnoServlet(Object instance, Method method, RequestMethod requestMethod) {
            ProxyInvoker<Object> invoker = Proxys.javassist().enhanceMethod(new MethodDefinition<>(instance, method));
            switch (requestMethod) {
                case GET:
                    return new MappingAnnoServlet(invoker, method) {
                        @Override
                        protected Object doGet(ServletRequest request, ServletResponse response) throws Throwable {
                            return invoker.invoke(parseParams(request, response));
                        }
                    };
                case POST:
                    return new MappingAnnoServlet(invoker, method) {
                        @Override
                        protected Object doPost(ServletRequest request, ServletResponse response) throws Throwable {
                            return invoker.invoke(parseParams(request, response));
                        }
                    };
                case PUT:
                    return new MappingAnnoServlet(invoker, method) {
                        @Override
                        protected void doPut(ServletRequest request, ServletResponse response) throws Throwable {
                            invoker.invoke(parseParams(request, response));
                        }
                    };
                case DELETE:
                    return new MappingAnnoServlet(invoker, method) {
                        @Override
                        protected void doDelete(ServletRequest request, ServletResponse response) throws Throwable {
                            invoker.invoke(parseParams(request, response));
                        }
                    };
            }

            throw new IllegalStateException("encounter unknown error");
        }

        public KinHttpServerBuilder sessionManager(Class<? extends HttpSessionManager> sessionManagerClass) {
            checkState();
            kinHttpServer.sessionManagerClass = sessionManagerClass;
            return this;
        }

        /**
         * 检查是否exported
         */
        private void checkState() {
            if (kinHttpServer.isExport) {
                throw new IllegalStateException("http server has bind!!! can not change");
            }
        }

        public void bind(InetSocketAddress address) {
            checkState();
            kinHttpServer.isExport = true;
            HttpServerTransportOption.builder()
                    .protocolHandler(new HttpServerProtocolHandler(kinHttpServer))
                    .bind(address);
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * servlet 配置
     */
    static class ServletConfig extends LazyInstantiation<Servlet> {
        /** mapping url */
        private final String path;
        /** http request method, null表示全匹配 */
        private final RequestMethod method;

        ServletConfig(String path, Class<? extends Servlet> servletClass) {
            this(path, servletClass, null);
        }

        ServletConfig(String path, Class<? extends Servlet> servletClass, RequestMethod method) {
            super(servletClass);
            this.path = path;
            this.method = method;
        }

        ServletConfig(String path, Servlet instance, RequestMethod method) {
            super(null);
            super.instance = instance;
            this.path = path;
            this.method = method;
        }

        //getter
        public String getPath() {
            return path;
        }

        public RequestMethod getMethod() {
            return method;
        }
    }

    /**
     * filter 配置
     */
    static class FilterConfig extends LazyInstantiation<Filter> {
        /** mapping url */
        private final String path;

        FilterConfig(String path, Class<? extends Filter> filterClass) {
            super(filterClass);
            this.path = path;
        }

        FilterConfig(String path, Filter instance) {
            super(null);
            super.instance = instance;
            this.path = path;
        }

        //getter
        public String getPath() {
            return path;
        }
    }
}
