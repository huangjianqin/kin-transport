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

    //----------------------------------------------------------------------------------------------------------------
    public static KinHttpServer create(String appName) {
        return new KinHttpServer(appName);
    }

    public static KinHttpServer create() {
        return new KinHttpServer("default");
    }

    //----------------------------------------------------------------------------------------------------------------
    public KinHttpServer mappingServlet(String url, Class<? extends Servlet> servletClass) {
        if (!isExport) {
            servletConfigs.add(new ServletConfig(url, servletClass));
        }
        return this;
    }

    public KinHttpServer mappingFilter(String url, Class<? extends Filter> filterClass) {
        if (!isExport) {
            filterConfigs.add(new FilterConfig(url, filterClass));
        }
        return this;
    }

    public KinHttpServer sessionManager(Class<? extends HttpSessionManager> sessionManagerClass) {
        if (!isExport) {
            this.sessionManagerClass = sessionManagerClass;
        }
        return this;
    }

    public void build(InetSocketAddress address) {
        if (isExport) {
            return;
        }
        new HttpServerTransportOption()
                .protocolHandler(new HttpServerProtocolHandler(this))
                .build(address);
        isExport = true;
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
