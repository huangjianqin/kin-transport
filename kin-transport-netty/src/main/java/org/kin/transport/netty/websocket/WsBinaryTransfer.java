package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
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
public class WsBinaryTransfer
        extends AbstractTransportProtocolTransfer<BinaryWebSocketFrame, SocketProtocol, BinaryWebSocketFrame> {
    private final SocketTransfer transfer;

    public WsBinaryTransfer(boolean compression, boolean serverElseClient) {
        super(compression);
        this.transfer = new SocketTransfer(compression, serverElseClient);
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        return transfer.decode(ctx, frame.content());
    }

    @Override
    public Collection<BinaryWebSocketFrame> encode(ChannelHandlerContext ctx, SocketProtocol protocol) throws Exception {
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
