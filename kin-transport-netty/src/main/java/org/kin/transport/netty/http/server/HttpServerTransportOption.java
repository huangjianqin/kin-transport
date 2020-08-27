package org.kin.transport.netty.http.server;

import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportOption;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerTransportOption extends TransportOption {
    private HttpServerTransportHandler transportHandler;

    public final Server http(InetSocketAddress address) {
        HttpServerHandlerInitializer httpServerHandlerInitializer = handlerInitializer();
        Server server = new Server(address);
        server.bind(this, httpServerHandlerInitializer);
        return server;
    }

    protected HttpServerHandlerInitializer handlerInitializer() {
        return new HttpServerHandlerInitializer(this);
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends HttpServerTransportOption> T transportHandler(HttpServerTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    //getter
    public HttpServerTransportHandler getTransportHandler() {
        return transportHandler;
    }
}
