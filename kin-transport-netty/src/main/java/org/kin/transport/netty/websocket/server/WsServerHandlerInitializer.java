package org.kin.transport.netty.websocket.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.kin.transport.netty.websocket.WsChannelHandlerInitializer;
import org.kin.transport.netty.websocket.WsTransportOption;
import org.kin.transport.netty.websocket.handler.ByteBuf2BinaryFrameEncoder;
import org.kin.transport.netty.websocket.handler.ByteBuf2TextFrameEncoder;
import org.kin.transport.netty.websocket.server.handler.WsServerHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * websocket server的channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public class WsServerHandlerInitializer extends WsChannelHandlerInitializer {

    public WsServerHandlerInitializer(WsTransportOption transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>();

        channelHandlers.add(new HttpServerCodec());
        channelHandlers.add(new HttpObjectAggregator(65536));
        if (transportOption.isCompression()) {
            channelHandlers.add(new WebSocketServerCompressionHandler());
        }
        //适配指定url
        channelHandlers.add(new WebSocketServerProtocolHandler(transportOption.getHandshakeUrl()));
        channelHandlers.add(new WsServerHandler(transportOption.getTransportHandler()));
        if (transportOption.isBinaryOrText()) {
            channelHandlers.add(new ByteBuf2BinaryFrameEncoder());
        } else {
            channelHandlers.add(new ByteBuf2TextFrameEncoder());
        }
        return channelHandlers;
    }
}
