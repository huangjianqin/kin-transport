package org.kin.transport.netty.http.client;

import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.Client;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientTransportOption extends AbstractTransportOption {
    private HttpClientTransportHandler transportHandler;

    public static ProtocolBaseHttpClientTransportOption protocol() {
        return new ProtocolBaseHttpClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
    public final <C extends Client> C http(InetSocketAddress address) {
        HttpClientHandlerInitializer httpClientHandlerInitializer = handlerInitializer();
        C client = client(address);
        client.connect(this, httpClientHandlerInitializer);
        return client;
    }

    protected HttpClientHandlerInitializer handlerInitializer() {
        return new HttpClientHandlerInitializer(this);
    }

    protected <C extends Client> C client(InetSocketAddress address) {
        return (C) new Client(address);
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
