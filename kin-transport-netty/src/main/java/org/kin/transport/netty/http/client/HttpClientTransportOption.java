package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
class HttpClientTransportOption
        extends AbstractTransportOption<FullHttpResponse, HttpEntity, FullHttpRequest, HttpClientTransportOption> {
    public final HttpClient build(InetSocketAddress address) {
        HttpClientHandlerInitializer httpClientHandlerInitializer = new HttpClientHandlerInitializer(this);
        HttpClient client = new HttpClient(address);
        client.connect(this, httpClientHandlerInitializer);
        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public int getGlobalRateLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransportProtocolTransfer<FullHttpResponse, HttpEntity, FullHttpRequest> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return new HttpClientTransfer(isCompression(), getGlobalRateLimit());
        }

        return super.getTransportProtocolTransfer();
    }
}
