package org.kin.transport.netty;

import java.util.EventListener;

/**
 * netty操作监听器
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public interface NettyOperationListener<C> extends EventListener {
    /**
     * 操作成功
     *
     * @param c 操作对象
     * @throws Exception 异常
     */
    default void onSuccess(C c) {
        //do nothing
    }

    /**
     * 操作失败
     *
     * @param c     操作对象
     * @param cause 异常
     * @throws Exception 异常
     */
    default void onFailure(C c, Throwable cause) {
        //do nothing
    }
}