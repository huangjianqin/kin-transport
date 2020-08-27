package org.kin.transport.netty.http.client;

import org.kin.transport.netty.Client;
import org.kin.transport.netty.ProtocolBaseClient;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.http.client.handler.ProtocolBaseHttpClientTransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpClientTransportOption extends HttpClientTransportOption {
    private TransportHandler<AbstractProtocol> protocolTransportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    public ProtocolBaseHttpClientTransportOption() {
        transportHandler(new ProtocolBaseHttpClientTransportHandler());
    }

    /**
     * 自定义实现WsTransportHandler, 不允许修改
     */
    @Override
    public <T extends HttpClientTransportOption> T transportHandler(HttpClientTransportHandler transportHandler) {
        return (T) this;
    }

    @Override
    protected HttpClientHandlerInitializer handlerInitializer() {
        return new ProtocolBaseHttpClientHandlerInitializer(
                this,
                protocolTransportHandler,
                protocolTransfer);
    }

    @Override
    protected <C extends Client> C client(InetSocketAddress address) {
        return (C) new ProtocolBaseClient(address);
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends ProtocolBaseHttpClientTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.protocolTransportHandler = transportHandler;
        return (T) this;
    }

    public <T extends ProtocolBaseHttpClientTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return (T) this;
    }

    //getter
    public TransportHandler<AbstractProtocol> getProtocolTransportHandler() {
        return protocolTransportHandler;
    }

    public ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
    }
}
