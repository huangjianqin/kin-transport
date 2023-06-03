package org.kin.transport.netty;

import reactor.netty.Connection;

/**
 * channel initializer
 * 初始化channel handlers
 *
 * @author huangjianqin
 * @date 2023/6/3
 */
public interface ChannelInitializer {
    ChannelInitializer DEFAULT = conn -> {
    };

    /**
     * 初始化channel handlers
     *
     * @param connection remote connection
     */
    void initChannel(Connection connection);
}

