package org.kin.transport.netty.websocket.server;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ChannelProtocolHandler;
import org.kin.transport.netty.socket.protocol.ProtocolCodec;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * websocket server的channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public class ProtocolBaseWsServerHandlerInitializer extends WsServerHandlerInitializer {
    private final TransportHandler<AbstractProtocol> transportHandler;
    private final ProtocolTransfer protocolTransfer;

    public ProtocolBaseWsServerHandlerInitializer(AbstractWsTransportOption transportOption,
                                                  TransportHandler<AbstractProtocol> transportHandler,
                                                  ProtocolTransfer protocolTransfer) {
        super(transportOption);
        this.transportHandler = transportHandler;
        this.protocolTransfer = protocolTransfer;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());
        channelHandlers.add(new ProtocolCodec(protocolTransfer, true, transportOption.isCompression()));
        channelHandlers.add(new ChannelProtocolHandler(transportHandler));
        return channelHandlers;
    }
}
