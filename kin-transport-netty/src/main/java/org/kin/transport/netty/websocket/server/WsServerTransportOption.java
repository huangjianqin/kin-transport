package org.kin.transport.netty.websocket.server;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsServerTransportOption<MSG, INOUT extends WebSocketFrame>
        extends AbstractWsTransportOption<MSG, INOUT, WsServerTransportOption<MSG, INOUT>> {
    public final Server build(InetSocketAddress address) {
        WsServerHandlerInitializer<MSG, INOUT> handlerInitializer = new WsServerHandlerInitializer<>(this);
        Server server = new Server(address);
        server.bind(this, handlerInitializer);
        return server;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<INOUT, MSG, INOUT> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return getDefaultTransportProtocolTransfer(true);
        }

        return super.getTransportProtocolTransfer();
    }
}
