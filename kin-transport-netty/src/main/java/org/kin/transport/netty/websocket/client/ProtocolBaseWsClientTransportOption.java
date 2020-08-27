package org.kin.transport.netty.websocket.client;

import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.WsTransportHandler;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;
import org.kin.transport.netty.websocket.handler.ProtocolBaseWsTransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseWsClientTransportOption extends WsClientTransportOption {
    private TransportHandler<AbstractProtocol> protocolTransportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    public ProtocolBaseWsClientTransportOption() {
        transportHandler(new ProtocolBaseWsTransportHandler());
    }

    /**
     * 自定义实现WsTransportHandler, 不允许修改
     */
    @Override
    public <T extends AbstractWsTransportOption> T transportHandler(WsTransportHandler transportHandler) {
        return (T) this;
    }

    @Override
    protected WsClientHandlerInitializer handlerInitializer(WsClientHandler wsClientHandler) {
        return new ProtocolBaseWsClientHandlerInitializer(
                this,
                wsClientHandler,
                protocolTransportHandler,
                protocolTransfer);
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends ProtocolBaseWsClientTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.protocolTransportHandler = transportHandler;
        return (T) this;
    }

    public <T extends ProtocolBaseWsClientTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
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
