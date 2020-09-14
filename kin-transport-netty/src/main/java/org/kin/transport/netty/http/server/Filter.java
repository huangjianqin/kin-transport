package org.kin.transport.netty.http.server;

/**
 * http请求处理filter, filter后才是servlet逻辑处理, servlet逻辑处理完后, response也要走一遍filter
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
public interface Filter {
    /**
     * 初始化
     */
    default void init() {

    }

    /**
     * 过滤逻辑实现
     *
     * @param request  请求
     * @param response 响应
     * @param chain    filter调用链
     */
    void doFilter(ServletRequest request, ServletResponse response);

    /**
     * destroy, 清理资源
     */
    default void destroy() {
    }
}
