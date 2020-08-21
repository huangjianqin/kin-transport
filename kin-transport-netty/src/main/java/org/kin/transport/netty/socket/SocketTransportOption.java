package org.kin.transport.netty.socket;

import org.kin.transport.netty.TransportOption;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class SocketTransportOption extends TransportOption {
    private TransportHandler transportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    /** server配置 */
    public static ServerTransportOption server() {
        return new ServerTransportOption();
    }

    /** client配置 */
    public static ClientTransportOption client() {
        return new ClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends SocketTransportOption> T transportHandler(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends SocketTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return (T) this;
    }

    //getter
    public TransportHandler getTransportHandler() {
        return transportHandler;
    }

    public ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
    }
}
