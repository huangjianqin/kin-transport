package org.kin.transport.netty.udp;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
import org.kin.transport.netty.utils.ChannelUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransportProtocolTransfer
        extends AbstractTransportProtocolTransfer<DatagramPacket, UdpProtocolWrapper, DatagramPacket> {
    private final SocketTransportProtocolTransfer transfer;
    /** 限流 */
    private final RateLimiter globalRateLimiter;

    public UdpTransportProtocolTransfer(boolean compression, boolean serverElseClient, int globalRateLimit) {
        super(compression);
        this.transfer = new SocketTransportProtocolTransfer(compression, serverElseClient);
        if (globalRateLimit > 0) {
            globalRateLimiter = RateLimiter.create(globalRateLimit);
        } else {
            globalRateLimiter = null;
        }
    }

    @Override
    public Collection<UdpProtocolWrapper> decode(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        if (ChannelUtils.globalRateLimit(ctx, globalRateLimiter)) {
            return Collections.emptyList();
        }
        Collection<SocketProtocol> protocols = transfer.decode(ctx, datagramPacket.content());
        return protocols.stream()
                .map(sp -> UdpProtocolWrapper.receiverWrapper(sp, datagramPacket.sender()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<DatagramPacket> encode(ChannelHandlerContext ctx, UdpProtocolWrapper wrapper) throws Exception {
        List<ByteBuf> byteBufs = new ArrayList<>(transfer.encode(ctx, wrapper.getProtocol()));
        List<DatagramPacket> datagramPackets = new ArrayList<>(byteBufs.size());
        for (ByteBuf byteBuf : byteBufs) {
            datagramPackets.add(new DatagramPacket(byteBuf, wrapper.getTargetAddress()));
        }

        return datagramPackets;
    }

    @Override
    public Class<DatagramPacket> getInClass() {
        return DatagramPacket.class;
    }

    @Override
    public Class<UdpProtocolWrapper> getMsgClass() {
        return UdpProtocolWrapper.class;
    }
}
