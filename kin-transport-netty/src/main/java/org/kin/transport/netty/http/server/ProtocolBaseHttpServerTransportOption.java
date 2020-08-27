package org.kin.transport.netty.http.server;

import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.http.server.handler.ProtocolBaseHttpServerTransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseHttpServerTransportOption extends HttpServerTransportOption {
    private TransportHandler<AbstractProtocol> protocolTransportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    public ProtocolBaseHttpServerTransportOption() {
        transportHandler(new ProtocolBaseHttpServerTransportHandler());
    }

    /**
     * 自定义实现WsTransportHandler, 不允许修改
     */
    @Override
    public <T extends HttpServerTransportOption> T transportHandler(HttpServerTransportHandler transportHandler) {
        return (T) this;
    }

    @Override
    protected HttpServerHandlerInitializer handlerInitializer() {
        return new ProtocolBaseHttpServerHandlerInitializer(
                this,
                protocolTransportHandler,
                protocolTransfer);
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends ProtocolBaseHttpServerTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.protocolTransportHandler = transportHandler;
        return (T) this;
    }

    public <T extends ProtocolBaseHttpServerTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
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
