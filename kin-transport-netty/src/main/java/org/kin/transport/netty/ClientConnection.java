package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * client抽象
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public abstract class ClientConnection extends AbstractConnection {
    public ClientConnection(AbstractTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    /**
     * 连接
     *
     * @param transportOption           client transport配置
     * @param channelHandlerInitializer netty channel handler 初始化
     */
    public abstract void connect(InetSocketAddress address);
}
