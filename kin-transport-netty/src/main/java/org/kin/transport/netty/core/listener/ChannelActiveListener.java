package org.kin.transport.netty.core.listener;

import io.netty.channel.Channel;

/**
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
@FunctionalInterface
public interface ChannelActiveListener {
    /**
     * 在channel线程调用
     * @param channel 触发listener的channel
     */
    void channelActive(Channel channel);
}
