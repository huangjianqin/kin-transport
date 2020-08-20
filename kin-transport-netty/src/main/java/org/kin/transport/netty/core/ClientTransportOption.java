package org.kin.transport.netty.core;

import org.kin.transport.netty.socket.SocketHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * client transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ClientTransportOption extends TransportOption {
    public Client tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, false);
        Client client = new Client(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }
}
