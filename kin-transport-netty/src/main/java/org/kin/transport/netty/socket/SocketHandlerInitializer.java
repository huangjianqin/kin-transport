package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.ChannelProtocolHandler;
import org.kin.transport.netty.socket.handler.ProtocolCodec;
import org.kin.transport.netty.socket.handler.SocketFrameCodec;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * socket的channel handler初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public class SocketHandlerInitializer extends AbstractChannelHandlerInitializer {
    private final AbstractSocketTransportOption transportOption;
    private final boolean serverElseClient;

    public SocketHandlerInitializer(AbstractSocketTransportOption transportOption, boolean serverElseClient) {
        this.transportOption = transportOption;
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        return serverElseClient ?
                Collections.singleton(SocketFrameCodec.serverFrameCodec(transportOption.getGlobalRateLimit())) :
                Collections.singleton(SocketFrameCodec.clientFrameCodec());
    }

    @Override
    public final ChannelHandler[] getChannelHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.addAll(firstHandlers());
        channelHandlers.add(new ProtocolCodec(transportOption.getProtocolTransfer(), serverElseClient, transportOption.isCompression()));
        channelHandlers.add(new ChannelProtocolHandler(transportOption.getTransportHandler()));
        channelHandlers.addAll(lastHandlers());

        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}
