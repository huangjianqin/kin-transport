package org.kin.transport.netty.socket.server;

import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransportOption;

import java.net.InetSocketAddress;

/**
 * server transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketServerTransportOption extends SocketTransportOption {
    public Server tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }
}
