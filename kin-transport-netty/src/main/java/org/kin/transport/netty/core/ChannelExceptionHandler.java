package org.kin.transport.netty.core;

import io.netty.channel.Channel;

/**
 * Created by huangjianqin on 2019/6/3.
 */
@FunctionalInterface
public interface ChannelExceptionHandler {
    /**
     * 在channel线程调用
     */
    void handleException(Channel channel, Throwable cause);
}
