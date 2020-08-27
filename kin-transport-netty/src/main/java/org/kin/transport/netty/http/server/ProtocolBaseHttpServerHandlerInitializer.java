package org.kin.transport.netty.http.server;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.handler.ChannelProtocolHandler;
import org.kin.transport.netty.socket.handler.ProtocolCodec;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpServerHandlerInitializer extends HttpServerHandlerInitializer {
    private final TransportHandler<AbstractProtocol> transportHandler;
    private final ProtocolTransfer protocolTransfer;

    public ProtocolBaseHttpServerHandlerInitializer(
            HttpServerTransportOption transportOption,
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