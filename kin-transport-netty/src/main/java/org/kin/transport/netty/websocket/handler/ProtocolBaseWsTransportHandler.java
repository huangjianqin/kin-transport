package org.kin.transport.netty.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.transport.netty.websocket.WsTransportHandler;

/**
 * 基于协议解析的web socket transport handler
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class ProtocolBaseWsTransportHandler extends WsTransportHandler {

    @Override
    public void handle(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //不管是text还是bytes, 直接当成bytes处理
        ctx.fireChannelRead(frame.content());
    }
}
