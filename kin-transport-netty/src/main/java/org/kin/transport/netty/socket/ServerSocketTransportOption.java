package org.kin.transport.netty.socket;

import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.websocket.WsServerHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ServerSocketTransportOption extends SocketTransportOption {
    public Server tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }

    public Server ws(InetSocketAddress address) {
        WsServerHandlerInitializer WSServerHandlerInitializer = new WsServerHandlerInitializer(this);
        Server server = new Server(address);
        server.bind(this, WSServerHandlerInitializer);
        return server;
    }
}
