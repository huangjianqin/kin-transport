package org.kin.transport.netty.socket.client;

import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.socket.AbstractSocketTransportOption;
import org.kin.transport.netty.socket.SocketHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * client transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class SocketClientTransportOption extends AbstractSocketTransportOption {
    public Client tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, false);
        Client client = new Client(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }
}
