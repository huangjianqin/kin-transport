package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.kin.framework.utils.CollectionUtils;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseClient extends Client {
    public ProtocolBaseClient(InetSocketAddress address) {
        super(address);
    }

    /**
     * 请求消息
     */
    public void request(AbstractProtocol protocol) {
        request(protocol);
    }

    /**
     * 请求消息
     */
    public void request(AbstractProtocol protocol, ChannelFutureListener... listeners) {
        if (isActive() && Objects.nonNull(protocol)) {
            ChannelFuture channelFuture = channel.writeAndFlush(protocol.write());
            if (CollectionUtils.isNonEmpty(listeners)) {
                channelFuture.addListeners(listeners);
            }
        }
    }

    @Override
    public void request(ByteBuf byteBuf, ChannelFutureListener... listeners) {
        throw new UnsupportedOperationException();
    }
}
