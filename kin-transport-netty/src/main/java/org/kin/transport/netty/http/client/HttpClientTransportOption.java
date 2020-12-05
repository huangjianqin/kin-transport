package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.CompressionType;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
class HttpClientTransportOption
        extends AbstractTransportOption<FullHttpResponse, HttpEntity, FullHttpRequest, HttpClientTransportOption> {
    public final HttpClient connect(InetSocketAddress address) {
        HttpClientHandlerInitializer httpClientHandlerInitializer = new HttpClientHandlerInitializer(this);
        HttpClient client = new HttpClient(this, httpClientHandlerInitializer);
        client.connect(address);
        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<FullHttpResponse, HttpEntity, FullHttpRequest> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return new HttpClientTransfer();
        }

        return super.getTransportProtocolTransfer();
    }

    //------------------------------------------------------builder------------------------------------------------------
    static HttpClientTransportOptionBuilder builder() {
        return new HttpClientTransportOptionBuilder();
    }

    static class HttpClientTransportOptionBuilder
            extends TransportOptionBuilder<FullHttpResponse, HttpEntity, FullHttpRequest, HttpClientTransportOption, HttpClientTransportOptionBuilder> {
        public HttpClientTransportOptionBuilder() {
            super(new HttpClientTransportOption());
        }

        @Override
        public HttpClientTransportOptionBuilder compress(CompressionType compressionType) {
            throw new UnsupportedOperationException("http compression set through http hearders");
        }

        @Override
        public HttpClientTransportOptionBuilder uncompress() {
            throw new UnsupportedOperationException("http compression set through http hearders");
        }
    }
}
