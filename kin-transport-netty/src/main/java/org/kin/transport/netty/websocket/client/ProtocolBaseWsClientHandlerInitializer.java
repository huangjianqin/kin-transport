package org.kin.transport.netty.websocket.client;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ChannelProtocolHandler;
import org.kin.transport.netty.socket.protocol.ProtocolCodec;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class ProtocolBaseWsClientHandlerInitializer extends WsClientHandlerInitializer {
    private final TransportHandler<AbstractProtocol> transportHandler;
    private final ProtocolTransfer protocolTransfer;

    public ProtocolBaseWsClientHandlerInitializer(AbstractWsTransportOption transportOption,
                                                  WsClientHandler wsClientHandler,
                                                  TransportHandler<AbstractProtocol> transportHandler,
                                                  ProtocolTransfer protocolTransfer) {
        super(transportOption, wsClientHandler);
        this.transportHandler = transportHandler;
        this.protocolTransfer = protocolTransfer;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());
        channelHandlers.add(new ProtocolCodec(protocolTransfer, false, transportOption.isCompression()));
        channelHandlers.add(new ChannelProtocolHandler(transportHandler));
        return channelHandlers;
    }
}

