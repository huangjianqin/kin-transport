package org.kin.transport.netty.core;

import io.netty.channel.Channel;

/**
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
@FunctionalInterface
public interface SessionBuilder {
    /**
     * 在channel线程调用
     * @param channel 连接channel
     * @return 绑定该channel的seesion
     */
    AbstractSession create(Channel channel);
}
