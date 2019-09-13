package org.kin.transport.netty.core;

import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.socket.SocketHandlerInitializer;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ServerTransportOption extends TransportOption {
    public Server tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, true);
        Server server = new Server(address);
        try {
            server.bind(getChannelOptions(), channelHandlerInitializer.getChannelHandlers());
        } catch (Exception e) {
            ExceptionUtils.log(e);
        }
        return server;
    }
}
