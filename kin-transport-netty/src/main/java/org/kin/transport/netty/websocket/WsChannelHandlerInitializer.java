package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;

import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class WsChannelHandlerInitializer extends AbstractChannelHandlerInitializer {
    protected final WsTransportOption transportOption;

    public WsChannelHandlerInitializer(WsTransportOption transportOption) {
        this.transportOption = transportOption;
    }

    @Override
    public ChannelHandler[] getChannelHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);
        channelHandlers.addAll(firstHandlers());
        channelHandlers.addAll(lastHandlers());
        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}
