package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * server抽象
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public abstract class ServerConnection extends AbstractConnection {
    public ServerConnection(AbstractTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    /**
     * 绑定端口
     */
    public abstract void bind(InetSocketAddress address);
}
