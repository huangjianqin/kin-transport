package org.kin.transport.netty;

import io.netty.channel.ChannelHandler;

import java.util.Collection;
import java.util.Collections;

/**
 * 增加前后handlers定义
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractChannelHandlerInitializer implements ChannelHandlerInitializer {
    /**
     * 前面的handlers
     */
    protected Collection<ChannelHandler> firstHandlers() {
        return Collections.emptyList();
    }

    /**
     * 后面的handlers
     */
    protected Collection<ChannelHandler> lastHandlers() {
        return Collections.emptyList();
    }
}
