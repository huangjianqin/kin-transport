package org.kin.transport.netty;

import java.util.EventListener;

/**
 * netty操作监听器
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public interface ChannelOperationListener extends EventListener {
    /**
     * 操作成功
     *
     * @param session 连接会话
     */
    default void onSuccess(Session session) {
        //do nothing
    }

    /**
     * 操作失败
     *
     * @param session 连接会话
     * @param cause   异常
     */
    default void onFailure(Session session, Throwable cause) {
        //do nothing
    }
}