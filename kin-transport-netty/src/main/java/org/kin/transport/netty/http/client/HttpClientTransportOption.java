package org.kin.transport.netty.http.client;

import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportOption;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientTransportOption extends TransportOption {
    private HttpClientTransportHandler transportHandler;

    public final Client http(InetSocketAddress address) {
        HttpClientHandlerInitializer httpClientHandlerInitializer = handlerInitializer();
        Client client = new Client(address);
        client.connect(this, httpClientHandlerInitializer);
        return client;
    }

    protected HttpClientHandlerInitializer handlerInitializer() {
        return new HttpClientHandlerInitializer(this);
    }

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
