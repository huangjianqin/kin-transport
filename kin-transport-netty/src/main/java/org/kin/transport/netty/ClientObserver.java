package org.kin.transport.netty;

import io.netty.handler.timeout.IdleStateEvent;

import javax.annotation.Nullable;

/**
 * client lifecycle listener
 *
 * @author huangjianqin
 * @date 2023/3/28
 */
public interface ClientObserver<C extends Client<C, ? extends AdvancedClientTransport<?>>> {
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
    default void onConnected(C client, Session session) {
        //default do nothing
    }

    /**
     * client connect fail时触发
     *
     * @param client client实例
     * @param cause  connect fail exception
     */
    default void onConnectFail(C client, Throwable cause) {
        //default do nothing
    }

    /**
     * client reconnected时触发
     *
     * @param client  client实例
     * @param session client session
     */
    default void onReconnected(C client, Session session) {
        //default do nothing
    }

    /**
     * client connection disconnected时触发
     * 一般来说优先于{@link #onDisposed(Client, Session)}执行
     *
     * @param client client实例
     */
    default void onDisconnected(C client, Session oldSession) {
        //default do nothing
    }

    /**
     * client disposed时触发
     *
     * @param client  client实例
     * @param session client session
     */
    default void onDisposed(C client, @Nullable Session session) {
        //default do nothing
    }
}
