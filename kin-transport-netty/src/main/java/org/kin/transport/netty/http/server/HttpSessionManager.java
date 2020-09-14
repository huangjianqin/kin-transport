package org.kin.transport.netty.http.server;

/**
 * 管理session
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
public interface HttpSessionManager {
    /**
     * 获取session
     */
    HttpSession session(String sessionId);

    /**
     * 移除指定sessionid的session
     */
    void removeSession(String sessionId);

    /**
     * destroy, 清理资源
     */
    default void destroy() {
    }
}
