package org.kin.transport.netty;

import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义 netty channel handler 初始化逻辑
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public interface ChannelHandlerInitializer<IN, MSG, OUT> {
    /**
     * 返回channel初始化需要的ChannelHandler
     *
     * @return 返回channel初始化需要的ChannelHandler
     */
    ChannelHandler[] getChannelHandlers();

    /**
     * 默认最前面的channel handler, 也就是{@link WriteTimeoutHandler} + {@link IdleStateHandler}
     */
    default <O extends AbstractTransportOption<IN, MSG, OUT, O>> List<ChannelHandler> setUpChannelHandlers(AbstractTransportOption<IN, MSG, OUT, O> transportOption) {
        List<ChannelHandler> channelHandlers = new ArrayList<>();

        int writeTimeout = transportOption.getWriteTimeout();
        if (writeTimeout > 0) {
            channelHandlers.add(new WriteTimeoutHandler(writeTimeout));
        }

        int readTimeout = transportOption.getReadTimeout();
        if (readTimeout > 0) {
            channelHandlers.add(new ReadTimeoutHandler(readTimeout));
        }

        int readIdleTime = transportOption.getReadIdleTime();
        int writeIdleTime = transportOption.getWriteIdleTime();
        int readWriteIdleTime = transportOption.getReadWriteIdleTime();
        if (readIdleTime > 0 || writeIdleTime > 0 || readWriteIdleTime > 0) {
            //其中一个>0就设置Handler
            channelHandlers.add(new IdleStateHandler(readIdleTime, writeIdleTime, readWriteIdleTime));
        }

        return channelHandlers;
    }
}
