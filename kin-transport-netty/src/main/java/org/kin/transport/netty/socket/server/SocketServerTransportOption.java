package org.kin.transport.netty.socket.server;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.AbstractSocketTransportOption;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketServerTransportOption extends AbstractSocketTransportOption<SocketServerTransportOption> {
    public Server tcp(InetSocketAddress address) {
        ChannelHandlerInitializer<ByteBuf, AbstractSocketProtocol, ByteBuf> channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<ByteBuf, AbstractSocketProtocol, ByteBuf> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() : new SocketTransportProtocolTransfer(isCompression(), true);
    }
}
