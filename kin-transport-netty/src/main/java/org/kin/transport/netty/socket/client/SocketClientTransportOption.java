package org.kin.transport.netty.socket.client;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * tcp client transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketClientTransportOption extends AbstractTransportOption<ByteBuf, SocketProtocol, ByteBuf, SocketClientTransportOption> {
    /**
     * 构建tcp client实例
     */
    public Client<SocketProtocol> connect(InetSocketAddress address) {
        ChannelHandlerInitializer<ByteBuf, SocketProtocol, ByteBuf> channelHandlerInitializer = new SocketHandlerInitializer<>(this, false);
        Client<SocketProtocol> client = new Client<>(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<ByteBuf, SocketProtocol, ByteBuf> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new SocketTransfer(false);
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static SocketClientTransportOptionBuilder builder() {
        return new SocketClientTransportOptionBuilder();
    }

    public static class SocketClientTransportOptionBuilder extends TransportOptionBuilder<ByteBuf, SocketProtocol, ByteBuf, SocketClientTransportOption> {
        public SocketClientTransportOptionBuilder() {
            super(new SocketClientTransportOption());
        }
    }
}
