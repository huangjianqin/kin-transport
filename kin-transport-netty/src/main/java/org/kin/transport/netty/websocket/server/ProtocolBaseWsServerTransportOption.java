package org.kin.transport.netty.websocket.server;

import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;
import org.kin.transport.netty.websocket.WsTransportHandler;
import org.kin.transport.netty.websocket.WsTransportOption;
import org.kin.transport.netty.websocket.handler.ProtocolBaseWsTransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseWsServerTransportOption extends WsServerTransportOption {
    private TransportHandler<AbstractProtocol> protocolTransportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    public ProtocolBaseWsServerTransportOption() {
        transportHandler(new ProtocolBaseWsTransportHandler());
    }

    /**
     * 自定义实现WsTransportHandler, 不允许修改
     */
    @Override
    public <T extends WsTransportOption> T transportHandler(WsTransportHandler transportHandler) {
        return (T) this;
    }

    @Override
    protected WsServerHandlerInitializer handlerInitializer() {
        return new ProtocolBaseWsServerHandlerInitializer(
                this,
                protocolTransportHandler,
                protocolTransfer
        );
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends ProtocolBaseWsServerTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.protocolTransportHandler = transportHandler;
        return (T) this;
    }

    public <T extends ProtocolBaseWsServerTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
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
