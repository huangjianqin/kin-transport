package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ChannelProtocolHandler;
import org.kin.transport.netty.socket.protocol.ProtocolCodec;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpClientHandlerInitializer extends HttpClientHandlerInitializer {
    private final TransportHandler<AbstractProtocol> transportHandler;
    private final ProtocolTransfer protocolTransfer;

    public ProtocolBaseHttpClientHandlerInitializer(
            HttpClientTransportOption transportOption,
            TransportHandler<AbstractProtocol> transportHandler,
            ProtocolTransfer protocolTransfer) {
        super(transportOption);
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
