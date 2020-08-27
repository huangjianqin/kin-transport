package org.kin.transport.netty.websocket.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.WsChannelHandlerInitializer;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;
import org.kin.transport.netty.websocket.handler.ByteBuf2BinaryFrameEncoder;
import org.kin.transport.netty.websocket.handler.ByteBuf2TextFrameEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsClientHandlerInitializer extends WsChannelHandlerInitializer {
    /** transport 配置 */
    private final WsClientHandler wsClientHandler;

    public WsClientHandlerInitializer(AbstractWsTransportOption transportOption, WsClientHandler wsClientHandler) {
        super(transportOption);
        this.wsClientHandler = wsClientHandler;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>();

        channelHandlers.add(new HttpServerCodec());
        channelHandlers.add(new HttpObjectAggregator(65536));
        if (transportOption.isCompression()) {
            channelHandlers.add(WebSocketClientCompressionHandler.INSTANCE);
        }
        channelHandlers.add(wsClientHandler);
        if (transportOption.isBinaryOrText()) {
            channelHandlers.add(new ByteBuf2BinaryFrameEncoder());
        } else {
            channelHandlers.add(new ByteBuf2TextFrameEncoder());
        }

        return channelHandlers;
    }
}

