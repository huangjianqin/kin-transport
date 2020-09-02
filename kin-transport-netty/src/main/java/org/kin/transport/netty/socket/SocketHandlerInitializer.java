package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.SocketFrameCodec;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * socket channel handler初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public class SocketHandlerInitializer<O extends AbstractSocketTransportOption<O>>
        extends AbstractChannelHandlerInitializer<ByteBuf, SocketProtocol, ByteBuf, O> {
    private final boolean serverElseClient;

    public SocketHandlerInitializer(O transportOption, boolean serverElseClient) {
        super(transportOption);
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());
        if (serverElseClient) {
            channelHandlers.add(SocketFrameCodec.serverFrameCodec(transportOption.getGlobalRateLimit()));
        } else {
            channelHandlers.add(SocketFrameCodec.clientFrameCodec());
        }

        return channelHandlers;
    }
}
