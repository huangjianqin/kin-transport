package org.kin.transport.netty.http.server;

import org.kin.framework.utils.ClassUtils;
import org.kin.transport.netty.Transports;

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
    private Class<? extends HttpSessionManager> sessionManagerClass = DefaultSessionManager.class;

    private KinHttpServer(String appName) {
        this.appName = appName;
    }

    //----------------------------------------------------------------------------------------------------------------
    public static KinHttpServer builder(String appName) {
        return new KinHttpServer(appName);
    }

    public static KinHttpServer builder() {
        return new KinHttpServer("default");
    }

    //----------------------------------------------------------------------------------------------------------------
    public KinHttpServer mappingServlet(String url, Class<? extends Servlet> servletClass) {
        servletConfigs.add(new ServletConfig(url, servletClass));
        return this;
    }

    public KinHttpServer mappingFilter(String url, Class<? extends Filter> filterClass) {
        filterConfigs.add(new FilterConfig(url, filterClass));
        return this;
    }

    public KinHttpServer sessionManager(Class<? extends HttpSessionManager> sessionManagerClass) {
        this.sessionManagerClass = sessionManagerClass;
        return this;
    }

    public void build(InetSocketAddress address) {
        Transports.http()
                .server(ServletTransportEntity.class)
                .protocolHandler(new HttpServerProtocolHandler(this))
                .build(address);
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

    //----------------------------------------------------------------------------------------------------------------

    /**
     * servlet 配置
     */
    static class ServletConfig {
        /** mapping url */
        private final String url;
        /** servlet class */
        private final Class<? extends Servlet> servletClass;

        ServletConfig(String url, Class<? extends Servlet> servletClass) {
            this.url = url;
            this.servletClass = servletClass;
        }

        //getter
        public String getUrl() {
            return url;
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
        private final String url;
        /** servlet class */
        private final Class<? extends Filter> filterClass;

        FilterConfig(String url, Class<? extends Filter> filterClass) {
            this.url = url;
            this.filterClass = filterClass;
        }

        //getter

        public String getUrl() {
            return url;
        }

        public Class<? extends Filter> getFilterClass() {
            return filterClass;
        }
    }
}
