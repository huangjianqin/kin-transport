package org.kin.transport.netty.socket.server;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * tcp server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketServerTransportOption extends AbstractTransportOption<ByteBuf, SocketProtocol, ByteBuf, SocketServerTransportOption> {
    /**
     * 构建tcp server实例
     */
    public Server bind(InetSocketAddress address) {
        ChannelHandlerInitializer<ByteBuf, SocketProtocol, ByteBuf> channelHandlerInitializer = new SocketHandlerInitializer<>(this, true);
        Server server = new Server(this, channelHandlerInitializer);
        server.bind(address);
        return server;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<ByteBuf, SocketProtocol, ByteBuf> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new SocketTransfer(true);
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static SocketServerTransportOptionBuilder builder() {
        return new SocketServerTransportOptionBuilder();
    }

    public static class SocketServerTransportOptionBuilder extends TransportOptionBuilder<ByteBuf, SocketProtocol, ByteBuf, SocketServerTransportOption> {
        public SocketServerTransportOptionBuilder() {
            super(new SocketServerTransportOption());
        }
    }
}
