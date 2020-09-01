package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.http.AbstractHttpTransportOption;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientTransportOption<MSG>
        extends AbstractHttpTransportOption<FullHttpResponse, MSG, FullHttpRequest, HttpClientTransportOption<MSG>> {
    public final Client<MSG> build(InetSocketAddress address) {
        HttpClientHandlerInitializer<MSG> httpClientHandlerInitializer = new HttpClientHandlerInitializer<>(this);
        Client<MSG> client = new Client<>(address);
        client.connect(this, httpClientHandlerInitializer);
        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpResponse, MSG, FullHttpRequest> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return (TransportProtocolTransfer<FullHttpResponse, MSG, FullHttpRequest>)
                    new HttpClientTransportProtocolTransfer(isCompression(), getGlobalRateLimit());
        }

        return super.getTransportProtocolTransfer();
    }
}
