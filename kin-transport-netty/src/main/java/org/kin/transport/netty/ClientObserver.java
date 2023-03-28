package org.kin.transport.netty;

import io.netty.handler.timeout.IdleStateEvent;

/**
 * client lifecycle listener
 *
 * @author huangjianqin
 * @date 2023/3/28
 */
public interface ClientObserver {
    /** 默认实现 */
    ClientObserver DEFAULT = new ClientObserver() {
    };

    /**
     * user自定义底层链接异常处理
     *
     * @param session 链接会话
     * @param cause   异常
     */
    default void onExceptionCaught(Session session, Throwable cause) {
        //default do nothing
    }

    /**
     * user自定义底层链接idle事件处理
     *
     * @param session 链接会话
     * @param event   idle event
     */
    default void onIdle(Session session, IdleStateEvent event) {
        //default do nothing
    }

    /**
     * user自定义事件处理
     *
     * @param session 链接会话
     * @param event   user event
     */
    default void onUserEventTriggered(Session session, Object event) {
        //default do nothing
    }

    /**
     * client connected时触发
     *
     * @param client  client实例
     * @param session client session
     */
    default <C extends Client<?>> void onConnected(C client, Session session) {
        //default do nothing
    }

    /**
     * client reconnected时触发
     *
     * @param client  client实例
     * @param session client session
     */
    default <C extends Client<?>> void onReconnected(C client, Session session) {
        //default do nothing
    }
}
