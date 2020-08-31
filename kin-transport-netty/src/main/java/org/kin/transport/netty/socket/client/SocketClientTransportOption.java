package org.kin.transport.netty.socket.client;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.AbstractSocketTransportOption;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * client transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketClientTransportOption extends AbstractSocketTransportOption<SocketClientTransportOption> {
    public Client<AbstractSocketProtocol> tcp(InetSocketAddress address) {
        ChannelHandlerInitializer<ByteBuf, AbstractSocketProtocol, ByteBuf> channelHandlerInitializer = new SocketHandlerInitializer(this, false);
        Client<AbstractSocketProtocol> client = new Client<>(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<ByteBuf, AbstractSocketProtocol, ByteBuf> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() : new SocketTransportProtocolTransfer(isCompression(), false);
    }
}
