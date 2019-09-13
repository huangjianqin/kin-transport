package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.core.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.core.TransportOption;
import org.kin.transport.netty.socket.handler.BaseFrameCodec;

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
                Collections.singleton(BaseFrameCodec.serverFrameCodec()) :
                Collections.singleton(BaseFrameCodec.clientFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return serverElseClient;
    }
}
