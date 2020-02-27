package org.kin.transport.netty.core;

import io.netty.channel.ChannelHandler;

/**
 * @author huangjianqin
 * @date 2019-09-12
 */
public interface ChannelHandlerInitializer {
    /**
     * 返回channel需要的ChannelHandler
     * @return 返回channel需要的ChannelHandler
     */
    ChannelHandler[] getChannelHandlers();
}
