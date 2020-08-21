package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.kin.transport.netty.core.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.core.TransportOption;
import org.kin.transport.netty.websocket.handler.ByteBuf2BinaryFrameCodec;
import org.kin.transport.netty.websocket.handler.WsServerhandler;

import java.util.Arrays;
import java.util.Collection;

/**
 * websocket server的channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public class BinaryWsServerHandlerInitializer extends AbstractChannelHandlerInitializer {
    public BinaryWsServerHandlerInitializer(TransportOption transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> beforeHandlers() {
        return Arrays.asList(
                new HttpServerCodec(),
                new HttpObjectAggregator(65536),
                new WebSocketServerCompressionHandler(),
                //适配指定url
                new WebSocketServerProtocolHandler(WSConstants.WS_PATH),
                new WsServerhandler(),
                new ByteBuf2BinaryFrameCodec());
    }

    @Override
    protected boolean serverElseClient() {
        return true;
    }
}