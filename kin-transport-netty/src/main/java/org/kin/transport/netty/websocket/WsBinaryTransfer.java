package org.kin.transport.netty.websocket;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
import org.kin.transport.netty.utils.ChannelUtils;

import java.util.Collection;
import java.util.Collections;
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
    /** 限流 */
    private final RateLimiter globalRateLimiter;

    public WsBinaryTransfer(boolean compression, boolean serverElseClient, int globalRateLimit) {
        super(compression);
        this.transfer = new SocketTransfer(compression, serverElseClient);
        if (globalRateLimit > 0) {
            globalRateLimiter = RateLimiter.create(globalRateLimit);
        } else {
            globalRateLimiter = null;
        }
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        if (ChannelUtils.globalRateLimit(ctx, globalRateLimiter)) {
            return Collections.emptyList();
        }
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
