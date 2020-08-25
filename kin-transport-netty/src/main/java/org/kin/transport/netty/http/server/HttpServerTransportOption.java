package org.kin.transport.netty.http.server;

import org.kin.transport.netty.TransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerTransportOption extends TransportOption {
    private HttpServerTransportHandler transportHandler;

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
