package org.kin.transport.netty.http.server;

import org.kin.framework.utils.ClassUtils;

import java.net.InetSocketAddress;
import java.util.Queue;
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

        public KinHttpServerBuilder(String appName) {
            checkState();
            this.kinHttpServer = new KinHttpServer(appName);
        }

        public KinHttpServerBuilder mappingServlet(String url, Class<? extends Servlet> servletClass) {
            checkState();
            kinHttpServer.servletConfigs.add(new ServletConfig(url, servletClass));
            return this;
        }

        public KinHttpServerBuilder mappingFilter(String url, Class<? extends Filter> filterClass) {
            checkState();
            kinHttpServer.filterConfigs.add(new FilterConfig(url, filterClass));
            return this;
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
    static class ServletConfig {
        /** mapping url */
        private final String path;
        /** servlet class */
        private final Class<? extends Servlet> servletClass;

        ServletConfig(String path, Class<? extends Servlet> servletClass) {
            this.path = path;
            this.servletClass = servletClass;
        }

        //getter
        public String getPath() {
            return path;
        }

        public Class<? extends Servlet> getServletClass() {
            return servletClass;
        }
    }

    /**
     * filter 配置
     */
    static class FilterConfig {
        /** mapping url */
        private final String path;
        /** servlet class */
        private final Class<? extends Filter> filterClass;

        FilterConfig(String path, Class<? extends Filter> filterClass) {
            this.path = path;
            this.filterClass = filterClass;
        }

        //getter

        public String getPath() {
            return path;
        }

        public Class<? extends Filter> getFilterClass() {
            return filterClass;
        }
    }
}
