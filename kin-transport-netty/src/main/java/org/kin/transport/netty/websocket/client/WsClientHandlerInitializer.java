package org.kin.transport.netty.websocket.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * websocket client channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/8/21
 */
public class WsClientHandlerInitializer<MSG, INOUT extends WebSocketFrame>
        extends AbstractChannelHandlerInitializer<INOUT, MSG, INOUT, WsClientTransportOption<MSG, INOUT>> {
    /** transport 配置 */
    private final WsClientHandler wsClientHandler;

    public WsClientHandlerInitializer(WsClientTransportOption<MSG, INOUT> transportOption, WsClientHandler wsClientHandler) {
        super(transportOption);
        this.wsClientHandler = wsClientHandler;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());

        channelHandlers.add(new HttpClientCodec());
        channelHandlers.add(new HttpObjectAggregator(65536));
        channelHandlers.add(WebSocketClientCompressionHandler.INSTANCE);
        channelHandlers.add(wsClientHandler);

        return channelHandlers;
    }
}

