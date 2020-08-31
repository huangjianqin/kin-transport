package org.kin.transport.netty.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 基于{@link SocketTransportProtocolTransfer}
 *
 * @author huangjianqin
 * @date 2020/8/31
 */
public class WsTransportProtocolTransfer
        extends AbstractTransportProtocolTransfer<BinaryWebSocketFrame, AbstractSocketProtocol, BinaryWebSocketFrame> {
    private final SocketTransportProtocolTransfer transfer;

    public WsTransportProtocolTransfer(boolean compression, boolean serverElseClient) {
        super(compression);
        this.transfer = new SocketTransportProtocolTransfer(compression, serverElseClient);
    }

    @Override
    public Collection<AbstractSocketProtocol> decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        return transfer.decode(ctx, frame.content());
    }

    @Override
    public Collection<BinaryWebSocketFrame> encode(ChannelHandlerContext ctx, AbstractSocketProtocol protocol) throws Exception {
        return transfer.encode(ctx, protocol).stream().map(BinaryWebSocketFrame::new).collect(Collectors.toList());
    }
}
