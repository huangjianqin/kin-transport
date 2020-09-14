package org.kin.transport.netty.http.server;

/**
 * @author huangjianqin
 * @date 2020/9/10
 */
interface Servlet {
    /**
     * 初始化
     */
    default void init() {
    }

    /**
     * 处理逻辑
     *
     * @param request  请求
     * @param response 响应
     */
    void service(ServletRequest request, ServletResponse response);

    /**
     * destroy, 清理资源
     */
    default void destroy() {
    }
}
