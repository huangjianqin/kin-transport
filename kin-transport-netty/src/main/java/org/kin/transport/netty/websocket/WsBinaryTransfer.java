package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * websocket协议转换
 * 基于{@link SocketTransfer}
 *
 * @author huangjianqin
 * @date 2020/8/31
 */
public class WsBinaryTransfer implements TransportProtocolTransfer<BinaryWebSocketFrame, SocketProtocol, BinaryWebSocketFrame> {
    private final SocketTransfer transfer;

    public WsBinaryTransfer(boolean serverElseClient) {
        this.transfer = new SocketTransfer(serverElseClient);
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        return transfer.decode(ctx, frame.content());
    }

    @Override
    public Collection<BinaryWebSocketFrame> encode(ChannelHandlerContext ctx, SocketProtocol protocol) {
        return transfer.encode(ctx, protocol).stream().map(BinaryWebSocketFrame::new).collect(Collectors.toList());
    }

    @Override
    public Class<BinaryWebSocketFrame> getInClass() {
        return BinaryWebSocketFrame.class;
    }

    @Override
    public Class<SocketProtocol> getMsgClass() {
        return SocketProtocol.class;
    }
}
