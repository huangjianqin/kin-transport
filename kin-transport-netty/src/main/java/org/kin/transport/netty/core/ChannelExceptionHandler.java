package org.kin.transport.netty.core;

import io.netty.channel.Channel;

/**
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
@FunctionalInterface
public interface ChannelExceptionHandler {
    /**
     * 在channel线程调用
     * @param channel 发生异常的channel
     * @param cause 具体异常
     */
    void handleException(Channel channel, Throwable cause);
}
