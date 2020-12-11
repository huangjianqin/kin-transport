package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.*;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;

/**
 * tcp server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketTransportOption extends AbstractTransportOption<ByteBuf, SocketProtocol, ByteBuf, SocketTransportOption>
        implements ServerOptionOprs<Server>, ClientOptionOprs<Client<SocketProtocol>> {
    /** 标识tcp server还是tcp client */
    private final boolean serverElseClient;
    /** 协议帧最大长度, 默认4M */
    private int maxFrameSize = 4 * 1024 * 1024;

    private SocketTransportOption(boolean serverElseClient) {
        this.serverElseClient = serverElseClient;
    }

    /**
     * 构建tcp server实例
     */
    @Override
    public Server bind(InetSocketAddress address) {
        if (!serverElseClient) {
            throw new UnsupportedOperationException("this is a tpc client transport options");
        }
        Server server = new Server(this, new SocketHandlerInitializer(this, serverElseClient));
        server.bind(address);
        return server;
    }

    /**
     * 构建tcp client实例
     */
    @Override
    public Client<SocketProtocol> connect(InetSocketAddress address) {
        if (serverElseClient) {
            throw new UnsupportedOperationException("this is a tpc server transport options");
        }

        Client<SocketProtocol> client = new Client<>(this, new SocketHandlerInitializer(this, serverElseClient));
        client.connect(address);
        return client;
    }

    /**
     * 构建支持自动重连的tcp client实例
     */
    @Override
    public Client<SocketProtocol> withReconnect(InetSocketAddress address) {
        return withReconnect(address, true);
    }

    /**
     * 构建支持自动重连的tcp client实例
     *
     * @param cacheMessage 是否缓存断开链接时发送的消息
     */
    @Override
    public Client<SocketProtocol> withReconnect(InetSocketAddress address, boolean cacheMessage) {
        if (serverElseClient) {
            throw new UnsupportedOperationException("this is a tpc server transport options");
        }

        ReconnectClient<SocketProtocol> client = new ReconnectClient<>(this, new ReconnectTransportOption<SocketProtocol>() {
            @Override
            public Client<SocketProtocol> reconnect(InetSocketAddress address) {
                return connect(address);
            }

            @Override
            public void wrapProtocolHandler(ProtocolHandler<SocketProtocol> protocolHandler) {
                SocketTransportOption.super.protocolHandler = protocolHandler;
            }
        }, cacheMessage);
        client.connect(address);
        return client;
    }

    //getter
    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    //------------------------------------------------------builder------------------------------------------------------

    /**
     * 通用builder
     */
    private static class SocketTransportOptionBuilder<B extends SocketTransportOptionBuilder<B>>
            extends TransportOptionBuilder<ByteBuf, SocketProtocol, ByteBuf, SocketTransportOption, B> {
        private SocketTransportOptionBuilder(boolean serverElseClient) {
            super(new SocketTransportOption(serverElseClient));
            //默认
            transportProtocolTransfer(new SocketTransfer(serverElseClient));
        }

        @SuppressWarnings("unchecked")
        public B maxFrameSize(int maxFrameSize) {
            checkState();
            transportOption.maxFrameSize = maxFrameSize;
            return (B) this;
        }
    }

    /**
     * server builder
     */
    public static class SocketServerTransportOptionBuilder extends SocketTransportOptionBuilder<SocketServerTransportOptionBuilder> implements ServerOptionOprs<Server> {
        public SocketServerTransportOptionBuilder() {
            super(true);
        }

        @Override
        public Server bind(InetSocketAddress address) {
            return build().bind(address);
        }
    }

    /**
     * client builder
     */
    public static class SocketClientTransportOptionBuilder extends SocketTransportOptionBuilder<SocketClientTransportOptionBuilder> implements ClientOptionOprs<Client<SocketProtocol>> {
        public SocketClientTransportOptionBuilder() {
            super(false);
        }

        @Override
        public Client<SocketProtocol> connect(InetSocketAddress address) {
            return build().connect(address);
        }

        @Override
        public Client<SocketProtocol> withReconnect(InetSocketAddress address) {
            return withReconnect(address, true);
        }

        @Override
        public Client<SocketProtocol> withReconnect(InetSocketAddress address, boolean cacheMessage) {
            return build().withReconnect(address, cacheMessage);
        }
    }
}
