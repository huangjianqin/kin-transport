package org.kin.transport.netty.core;

import io.netty.channel.ChannelHandler;

/**
 * @author huangjianqin
 * @date 2019-09-12
 */
public interface ChannelHandlerInitializer {
    ChannelHandler[] getChannelHandlers();
}
