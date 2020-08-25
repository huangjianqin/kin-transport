package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.ChannelProtocolHandler;
import org.kin.transport.netty.socket.handler.ProtocolCodec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2019-09-12
 */
public abstract class SocketChannelHandlerInitializer implements ChannelHandlerInitializer {
    protected final SocketTransportOption transportOption;

    public SocketChannelHandlerInitializer(SocketTransportOption transportOption) {
        this.transportOption = transportOption;
    }

    /**
     * 解析协议前 需要的handlers
     */
    protected Collection<ChannelHandler> beforeHandlers() {
        return Collections.emptyList();
    }

    /**
     * 处理完请求协议之后 需要的handlers
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
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.addAll(beforeHandlers());
        channelHandlers.add(new ProtocolCodec(transportOption.getProtocolTransfer(), serverElseClient(), transportOption.isCompression()));
        channelHandlers.add(new ChannelProtocolHandler(transportOption.getTransportHandler()));
        channelHandlers.addAll(afterHandlers());

        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}
