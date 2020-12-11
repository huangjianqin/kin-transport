package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.*;

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
        Server server = new Server(this, httpServerHandlerInitializer);
        server.bind(address);
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

    static class HttpServerTransportOptionBuilder
            extends TransportOptionBuilder<FullHttpRequest, ServletTransportEntity, FullHttpResponse, HttpServerTransportOption, HttpServerTransportOptionBuilder>
            implements ServerOptionOprs<Server> {
        public HttpServerTransportOptionBuilder() {
            super(new HttpServerTransportOption());
        }

        @Override
        public HttpServerTransportOptionBuilder compress(CompressionType compressionType) {
            checkState();
            throw new UnsupportedOperationException("http compression set through http hearders");
        }

        @Override
        public HttpServerTransportOptionBuilder uncompress() {
            checkState();
            throw new UnsupportedOperationException("http compression set through http hearders");
        }

        @Override
        public Server bind(InetSocketAddress address) {
            return build().bind(address);
        }
    }
}
