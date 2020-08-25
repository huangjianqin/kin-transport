package org.kin.transport.netty.socket;

import org.kin.transport.netty.TransportOption;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class SocketTransportOption extends TransportOption<AbstractProtocol> {
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
    public <T extends SocketTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return (T) this;
    }

    //getter
    public ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
    }
}
