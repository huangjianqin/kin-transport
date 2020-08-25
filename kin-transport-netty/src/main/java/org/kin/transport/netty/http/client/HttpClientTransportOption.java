package org.kin.transport.netty.http.client;

import org.kin.transport.netty.TransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientTransportOption extends TransportOption {
    private HttpClientTransportHandler transportHandler;

    //----------------------------------------------------------------------------------------------------------------
    public <T extends HttpClientTransportOption> T transportHandler(HttpClientTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    //getter
    public HttpClientTransportHandler getTransportHandler() {
        return transportHandler;
    }
}
