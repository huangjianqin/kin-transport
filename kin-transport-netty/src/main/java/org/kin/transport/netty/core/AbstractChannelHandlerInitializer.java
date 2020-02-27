package org.kin.transport.netty.core;

import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.kin.transport.netty.core.handler.ChannelIdleHandler;
import org.kin.transport.netty.core.handler.ChannelProtocolHandler;
import org.kin.transport.netty.core.handler.ProtocolCodec;
import org.kin.transport.netty.core.listener.ChannelIdleListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2019-09-12
 */
public abstract class AbstractChannelHandlerInitializer implements ChannelHandlerInitializer {
    protected final TransportOption transportOption;

    public AbstractChannelHandlerInitializer(TransportOption transportOption) {
        this.transportOption = transportOption;
    }

    /**
     * 返回解析协议需要的handlers
     */
    protected Collection<ChannelHandler> beforeHandlers() {
        return Collections.emptyList();
    }

    /**
     * 返回处理完请求协议之后需要执行的handlers
     */
    protected Collection<ChannelHandler> afterHandlers() {
        return Collections.emptyList();
    }

    /**
     * 标识是用于server还是client
     *
     * @return server -> true, client -> false
     */
    protected abstract boolean serverElseClient();

    @Override
    public final ChannelHandler[] getChannelHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>();
        channelHandlers.add(new WriteTimeoutHandler(3));


        ChannelIdleListener channelIdleListener = transportOption.getChannelIdleListener();
        if (channelIdleListener != null) {
            int readIdleTime = channelIdleListener.readIdleTime();
            int writeIdleTime = channelIdleListener.writeIdelTime();
            int allIdleTime = channelIdleListener.allIdleTime();
            if (readIdleTime > 0 || writeIdleTime > 0 || allIdleTime > 0) {
                //其中一个>0就设置Handler
                channelHandlers.add(new IdleStateHandler(readIdleTime, writeIdleTime, allIdleTime));
                channelHandlers.add(new ChannelIdleHandler(channelIdleListener));
            }

        }

        channelHandlers.addAll(beforeHandlers());
        channelHandlers.add(new ProtocolCodec(transportOption.getProtocolTransfer(), serverElseClient(), transportOption.isCompression()));
        channelHandlers.add(new ChannelProtocolHandler(
                transportOption.getProtocolHandler(),
                transportOption.getSessionBuilder(),
                transportOption.getChannelActiveListener(),
                transportOption.getChannelInactiveListener(),
                transportOption.getChannelExceptionHandler()));
        channelHandlers.addAll(afterHandlers());

        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}
