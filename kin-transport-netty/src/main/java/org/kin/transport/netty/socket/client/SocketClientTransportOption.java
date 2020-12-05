package org.kin.transport.netty.socket.client;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.*;
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
        Client<SocketProtocol> client = new Client<>(this, channelHandlerInitializer);
        client.connect(address);
        return client;
    }

    /**
     * 构建支持自动重连的tcp client实例
     */
    public Client<SocketProtocol> withReconnect(InetSocketAddress address) {
        return withReconnect(address, true);
    }

    /**
     * 构建支持自动重连的tcp client实例
     *
     * @param cacheMessage 是否缓存断开链接时发送的消息
     */
    public Client<SocketProtocol> withReconnect(InetSocketAddress address, boolean cacheMessage) {
        ReconnectClient<SocketProtocol> client = new ReconnectClient<>(this, new ReconnectTransportOption<SocketProtocol>() {
            @Override
            public Client<SocketProtocol> reconnect(InetSocketAddress address) {
                return connect(address);
            }

            @Override
            public void wrapProtocolHandler(ProtocolHandler<SocketProtocol> protocolHandler) {
                SocketClientTransportOption.super.protocolHandler = protocolHandler;
            }
        }, cacheMessage);
        client.connect(address);
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

    public static class SocketClientTransportOptionBuilder
            extends TransportOptionBuilder<ByteBuf, SocketProtocol, ByteBuf, SocketClientTransportOption, SocketClientTransportOptionBuilder> {
        public SocketClientTransportOptionBuilder() {
            super(new SocketClientTransportOption());
        }
    }
}
