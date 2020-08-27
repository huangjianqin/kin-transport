package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * server抽象
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public abstract class ServerConnection extends AbstractConnection {
    public ServerConnection(InetSocketAddress address) {
        super(address);
    }

    /**
     * 绑定
     *
     * @param transportOption           server transport配置
     * @param channelHandlerInitializer netty channel handler 初始化
     * @throws Exception 异常
     */
    public abstract void bind(TransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer);
}
