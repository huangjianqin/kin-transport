package org.kin.transport.netty;

import io.netty.handler.timeout.IdleStateEvent;

/**
 * server lifecycle listener
 *
 * @author huangjianqin
 * @date 2023/3/27
 */
public interface ServerLifecycle {
    /** 默认实现 */
    ServerLifecycle DEFAULT = new ServerLifecycle() {
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
     * server bound时触发
     *
     * @param server server实例
     */
    default <S extends Server<?>> void onBound(S server) {
        //default do nothing
    }

    /**
     * client connected时触发
     *
     * @param server  server实例
     * @param session client session
     */
    default <S extends Server<?>> void onClientConnected(S server, Session session) {
        //default do nothing
    }
}
