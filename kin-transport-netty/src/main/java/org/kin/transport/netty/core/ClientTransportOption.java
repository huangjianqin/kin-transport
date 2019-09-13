package org.kin.transport.netty.core;

import org.kin.transport.netty.socket.SocketHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ClientTransportOption extends TransportOption {
    public Client tcp(InetSocketAddress address){
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Client client = new Client(address);
        client.connect(getChannelOptions(), channelHandlerInitializer.getChannelHandlers());
        return client;
    }
}
