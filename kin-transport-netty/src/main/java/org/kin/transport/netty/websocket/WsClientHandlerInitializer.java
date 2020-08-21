package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.kin.transport.netty.socket.SocketChannelHandlerInitializer;
import org.kin.transport.netty.socket.SocketTransportOption;
import org.kin.transport.netty.websocket.handler.ByteBuf2BinaryFrameCodec;
import org.kin.transport.netty.websocket.handler.WsClientHandler;
import org.kin.transport.netty.websocket.handler.WsServerHandler;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsClientHandlerInitializer extends SocketChannelHandlerInitializer {
    private final WsClientHandler wsClientHandler;

    public WsClientHandlerInitializer(SocketTransportOption transportOption, WsClientHandler wsClientHandler) {
        super(transportOption);
        this.wsClientHandler = wsClientHandler;
    }

    @Override
    protected Collection<ChannelHandler> beforeHandlers() {
        return Arrays.asList(
                new HttpServerCodec(),
                new HttpObjectAggregator(65536),
                WebSocketClientCompressionHandler.INSTANCE,
                wsClientHandler,
                new WsServerHandler(),
                new ByteBuf2BinaryFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return false;
    }
}

