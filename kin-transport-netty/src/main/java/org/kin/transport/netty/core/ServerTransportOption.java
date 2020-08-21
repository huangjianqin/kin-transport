package org.kin.transport.netty.core;

import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.websocket.BinaryWsServerHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ServerTransportOption extends TransportOption {
    public Server tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }

    public Server ws(InetSocketAddress address) {
        BinaryWsServerHandlerInitializer binaryWSServerHandlerInitializer = new BinaryWsServerHandlerInitializer(this);
        Server server = new Server(address);
        server.bind(this, binaryWSServerHandlerInitializer);
        return server;
    }
}
