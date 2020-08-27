package org.kin.transport.netty.websocket.server;

import org.kin.transport.netty.Server;
import org.kin.transport.netty.websocket.WsTransportOption;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsServerTransportOption extends WsTransportOption {
    public final Server ws(InetSocketAddress address) {
        WsServerHandlerInitializer WSServerHandlerInitializer = handlerInitializer();
        Server server = new Server(address);
        server.bind(this, WSServerHandlerInitializer);
        return server;
    }

    protected WsServerHandlerInitializer handlerInitializer() {
        return new WsServerHandlerInitializer(this);
    }

}
