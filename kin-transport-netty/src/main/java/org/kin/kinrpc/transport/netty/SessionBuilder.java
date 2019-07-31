package org.kin.kinrpc.transport.netty;

import io.netty.channel.Channel;

/**
 * Created by huangjianqin on 2019/5/30.
 */
@FunctionalInterface
public interface SessionBuilder {
    /**
     * 在channel线程调用
     */
    AbstractSession create(Channel channel);
}
