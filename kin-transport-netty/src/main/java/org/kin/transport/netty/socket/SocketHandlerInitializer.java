package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.core.TransportOption;
import org.kin.transport.netty.core.handler.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.ByteFrameCodec;

import java.util.Collection;
import java.util.Collections;

/**
 * @author huangjianqin
 * @date 2019-09-12
 */
public class SocketHandlerInitializer extends AbstractChannelHandlerInitializer {
    private final boolean serverElseClient;

    public SocketHandlerInitializer(TransportOption transportOption, boolean serverElseClient) {
        super(transportOption);
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected Collection<ChannelHandler> beforeHandlers() {
        return serverElseClient ?
                Collections.singleton(ByteFrameCodec.serverFrameCodec(transportOption.getGlobalRateLimit())) :
                Collections.singleton(ByteFrameCodec.clientFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return serverElseClient;
    }
}
