package org.kin.transport.netty.socket;

import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.TransportHandler;
import org.kin.transport.netty.socket.client.SocketClientTransportOption;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.ProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocolTransfer;
import org.kin.transport.netty.socket.server.SocketServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public abstract class AbstractSocketTransportOption extends AbstractTransportOption {
    public static final AbstractSocketTransportOption INSTANCE = new AbstractSocketTransportOption() {
    };

    private TransportHandler<AbstractProtocol> transportHandler = TransportHandler.DO_NOTHING;
    private ProtocolTransfer protocolTransfer = SocketProtocolTransfer.instance();

    /** server配置 */
    public static SocketServerTransportOption server() {
        return new SocketServerTransportOption();
    }

    /** client配置 */
    public static SocketClientTransportOption client() {
        return new SocketClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends AbstractSocketTransportOption> T transportHandler(TransportHandler<AbstractProtocol> transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends AbstractSocketTransportOption> T protocolTransfer(ProtocolTransfer transfer) {
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
