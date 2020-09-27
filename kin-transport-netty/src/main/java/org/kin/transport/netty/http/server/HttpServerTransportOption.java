package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerTransportOption
        extends AbstractTransportOption<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption> {
    public final Server build(InetSocketAddress address) {
        HttpServerHandlerInitializer httpServerHandlerInitializer = new HttpServerHandlerInitializer(this);
        Server server = new Server(address);
        server.bind(this, httpServerHandlerInitializer);
        return server;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpRequest, ServletTransportEntity, FullHttpResponse> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return new HttpServerTransfer(isCompression());
        }

        return super.getTransportProtocolTransfer();
    }
}
