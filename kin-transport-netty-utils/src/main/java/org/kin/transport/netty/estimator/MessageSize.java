package org.kin.transport.netty.estimator;

/**
 * 供消息实现, 返回消息体的大小
 *
 * @author huangjianqin
 * @date 2021/12/7
 */
@FunctionalInterface
public interface MessageSize {
    /** 返回消息体的大小 */
    int size();
}
