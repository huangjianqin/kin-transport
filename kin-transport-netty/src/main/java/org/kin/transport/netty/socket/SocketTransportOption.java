package org.kin.transport.netty.socket;

import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.TransportOption;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class SocketTransportOption extends TransportOption {
    private TransportHandler<AbstractProtocol> transportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    /** server配置 */
    public static ServerSocketTransportOption server() {
        return new ServerSocketTransportOption();
    }

    /** client配置 */
    public static ClientSocketTransportOption client() {
        return new ClientSocketTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends SocketTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends SocketTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return (T) this;
    }

    //getter
    public TransportHandler<AbstractProtocol> getTransportHandler() {
        return transportHandler;
    }

    public ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
    }
}
