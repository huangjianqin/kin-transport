package org.kin.transport.netty.websocket.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.websocket.server.handler.WsServerHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * websocket server channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public class WsServerHandlerInitializer<MSG, INOUT extends WebSocketFrame>
        extends AbstractChannelHandlerInitializer<INOUT, MSG, INOUT, WsServerTransportOption<MSG, INOUT>> {

    public WsServerHandlerInitializer(WsServerTransportOption<MSG, INOUT> transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());

        channelHandlers.add(new HttpServerCodec());
        channelHandlers.add(new HttpObjectAggregator(65536));
        channelHandlers.add(new WebSocketServerCompressionHandler());
        //适配指定url
        //会对websocketframe进行引用release, 后面handler处理不能再release websocketframe
        channelHandlers.add(new WebSocketServerProtocolHandler(transportOption.getHandshakeUrl(), null, true));
        channelHandlers.add(new WsServerHandler());
        return channelHandlers;
    }
}
