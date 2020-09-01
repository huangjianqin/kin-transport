package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.framework.utils.ClassUtils;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.http.AbstractHttpTransportOption;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerTransportOption<MSG>
        extends AbstractHttpTransportOption<FullHttpRequest, MSG, FullHttpResponse, HttpServerTransportOption<MSG>> {
    public final Server build(InetSocketAddress address) {
        HttpServerHandlerInitializer<MSG> httpServerHandlerInitializer = new HttpServerHandlerInitializer<>(this);
        Server server = new Server(address);
        server.bind(this, httpServerHandlerInitializer);
        return server;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpRequest, MSG, FullHttpResponse> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            List<Class<?>> genericTypes = ClassUtils.getSuperClassGenericActualTypes(getClass());
            if (SocketProtocol.class.isAssignableFrom(genericTypes.get(1))) {
                return (TransportProtocolTransfer<FullHttpRequest, MSG, FullHttpResponse>)
                        new HttpServerTransportProtocolTransfer(isCompression(), getGlobalRateLimit());
            }
        }

        return super.getTransportProtocolTransfer();
    }
}
