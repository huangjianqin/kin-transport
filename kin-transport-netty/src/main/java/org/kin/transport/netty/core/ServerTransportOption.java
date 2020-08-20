package org.kin.transport.netty.core;

import org.kin.transport.netty.socket.SocketHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ServerTransportOption extends TransportOption {
    public Server tcp(InetSocketAddress address) throws Exception {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }
}
