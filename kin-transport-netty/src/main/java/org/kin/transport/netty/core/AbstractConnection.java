package org.kin.transport.netty.core;

import io.netty.channel.ChannelOption;
import org.kin.transport.netty.core.handler.ChannelHandlerInitializer;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 *
 * @author 健勤
 * @date 2017/2/10
 */
public abstract class AbstractConnection {
    protected final InetSocketAddress address;

    public AbstractConnection(InetSocketAddress address) {
        this.address = address;
    }

    /**
     * 连接
     * @param channelOptions            netty channel可选项
     * @param channelHandlerInitializer netty channel handler 初始化
     */
    public abstract void connect(Map<ChannelOption, Object> channelOptions, ChannelHandlerInitializer channelHandlerInitializer);

    /**
     * 绑定
     * @param channelOptions netty channel可选项
     * @param channelHandlerInitializer netty channel handler 初始化
     * @throws Exception 异常
     */
    public abstract void bind(Map<ChannelOption, Object> channelOptions, ChannelHandlerInitializer channelHandlerInitializer) throws Exception;

    /**
     * 连接关闭
     */
    public abstract void close();

    public String getAddress() {
        return address.getHostName() + ":" + address.getPort();
    }

    /**
     * 检查连接是否有效
     * @return 连接是否有效
     */
    public abstract boolean isActive();
}
