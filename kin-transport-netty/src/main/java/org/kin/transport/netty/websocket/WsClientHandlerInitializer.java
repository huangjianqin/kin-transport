package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.kin.transport.netty.core.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.core.TransportOption;
import org.kin.transport.netty.websocket.handler.ByteBuf2BinaryFrameCodec;
import org.kin.transport.netty.websocket.handler.WSClientHandler;
import org.kin.transport.netty.websocket.handler.WsServerhandler;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsClientHandlerInitializer extends AbstractChannelHandlerInitializer {
    private final WSClientHandler wsClientHandler;

    public WsClientHandlerInitializer(TransportOption transportOption, WSClientHandler wsClientHandler) {
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
                new WsServerhandler(),
                new ByteBuf2BinaryFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return false;
    }
}

