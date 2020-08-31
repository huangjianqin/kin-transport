package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.SocketFrameCodec;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.util.Collection;
import java.util.Collections;

/**
 * socket的channel handler初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public class SocketHandlerInitializer extends AbstractChannelHandlerInitializer<ByteBuf, AbstractSocketProtocol, ByteBuf, AbstractSocketTransportOption> {
    private final boolean serverElseClient;

    public SocketHandlerInitializer(AbstractSocketTransportOption transportOption, boolean serverElseClient) {
        super(transportOption);
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        return serverElseClient ?
                Collections.singleton(SocketFrameCodec.serverFrameCodec(transportOption.getGlobalRateLimit())) :
                Collections.singleton(SocketFrameCodec.clientFrameCodec());
    }
}
