package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.CompressionType;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
class HttpServerTransportOption
        extends AbstractTransportOption<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption> {
    final Server bind(InetSocketAddress address) {
        HttpServerHandlerInitializer httpServerHandlerInitializer = new HttpServerHandlerInitializer(this);
        Server server = new Server(address);
        server.bind(this, httpServerHandlerInitializer);
        return server;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpRequest, ServletTransportEntity, FullHttpResponse> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return new HttpServerTransfer();
        }

        return super.getTransportProtocolTransfer();
    }

    //------------------------------------------------------builder------------------------------------------------------
    static HttpServerTransportOptionBuilder builder() {
        return new HttpServerTransportOptionBuilder();
    }

    static class HttpServerTransportOptionBuilder extends TransportOptionBuilder<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption> {
        public HttpServerTransportOptionBuilder() {
            super(new HttpServerTransportOption());
        }

        @Override
        public TransportOptionBuilder<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption> compress(CompressionType compressionType) {
            throw new UnsupportedOperationException("http compression set through http hearders");
        }

        @Override
        public TransportOptionBuilder<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption> uncompress() {
            throw new UnsupportedOperationException("http compression set through http hearders");
        }
    }
}
