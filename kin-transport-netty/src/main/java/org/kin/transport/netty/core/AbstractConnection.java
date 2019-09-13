package org.kin.transport.netty.core;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Created by 健勤 on 2017/2/10.
 */
public abstract class AbstractConnection {
    protected final InetSocketAddress address;

    public AbstractConnection(InetSocketAddress address) {
        this.address = address;
    }

    public abstract void connect(Map<ChannelOption, Object> channelOptions, ChannelHandler[] channelHandlers);

    public abstract void bind(Map<ChannelOption, Object> channelOptions, ChannelHandler[] channelHandlers) throws Exception;

    public abstract void close();

    public String getAddress() {
        return address.getHostName() + ":" + address.getPort();
    }

    public abstract boolean isActive();
}
