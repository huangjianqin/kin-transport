package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.framework.utils.ClassUtils;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.http.AbstractHttpTransportOption;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientTransportOption<MSG>
        extends AbstractHttpTransportOption<FullHttpResponse, MSG, FullHttpRequest, HttpClientTransportOption<MSG>> {
    public final Client<MSG> http(InetSocketAddress address) {
        HttpClientHandlerInitializer<MSG> httpClientHandlerInitializer = new HttpClientHandlerInitializer<>(this);
        Client<MSG> client = new Client<>(address);
        client.connect(this, httpClientHandlerInitializer);
        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpResponse, MSG, FullHttpRequest> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            List<Class<?>> genericTypes = ClassUtils.getSuperClassGenericActualTypes(getClass());
            if (AbstractSocketProtocol.class.isAssignableFrom(genericTypes.get(1))) {
                return (TransportProtocolTransfer<FullHttpResponse, MSG, FullHttpRequest>) new HttpClientTransportProtocolTransfer(isCompression());
            }
        }

        return super.getTransportProtocolTransfer();
    }
}
