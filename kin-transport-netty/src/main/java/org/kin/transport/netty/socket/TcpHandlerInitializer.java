package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.socket.handler.SocketFrameCodec;

import java.util.Collection;
import java.util.Collections;

/**
 * socket的channel handler初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public class TcpHandlerInitializer extends SocketChannelHandlerInitializer {
    private final boolean serverElseClient;

    public TcpHandlerInitializer(SocketTransportOption transportOption, boolean serverElseClient) {
        super(transportOption);
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected Collection<ChannelHandler> beforeHandlers() {
        return serverElseClient ?
                Collections.singleton(SocketFrameCodec.serverFrameCodec(transportOption.getGlobalRateLimit())) :
                Collections.singleton(SocketFrameCodec.clientFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return serverElseClient;
    }
}
