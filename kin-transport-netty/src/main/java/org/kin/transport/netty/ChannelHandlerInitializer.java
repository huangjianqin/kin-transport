package org.kin.transport.netty;

import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * channel handler 初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public interface ChannelHandlerInitializer<IN, MSG, OUT> {
    /**
     * 返回channel需要的ChannelHandler
     *
     * @return 返回channel需要的ChannelHandler
     */
    ChannelHandler[] getChannelHandlers();

    default <O extends AbstractTransportOption<IN, MSG, OUT, O>> List<ChannelHandler> setUpChannelHandlers(AbstractTransportOption<IN, MSG, OUT, O> transportOption) {
        List<ChannelHandler> channelHandlers = new ArrayList<>();
        channelHandlers.add(new WriteTimeoutHandler(3));

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
