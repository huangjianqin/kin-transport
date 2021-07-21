package org.kin.transport.netty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransfer;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * udp协议转换
 * 基于{@link SocketTransfer}
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransfer implements TransportProtocolTransfer<DatagramPacket, UdpProtocolDetails, DatagramPacket> {
    private final SocketTransfer transfer;

    public UdpTransfer(boolean serverElseClient) {
        this.transfer = new SocketTransfer(serverElseClient);
    }

    @Override
    public Collection<UdpProtocolDetails> decode(ChannelHandlerContext ctx, DatagramPacket datagramPacket) {
        Collection<SocketProtocol> protocols = transfer.decode(ctx, datagramPacket.content());
        return protocols.stream()
                .map(sp -> UdpProtocolDetails.receiverWrapper(sp, datagramPacket.sender()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<DatagramPacket> encode(ChannelHandlerContext ctx, UdpProtocolDetails wrapper) {
        List<ByteBuf> byteBufs = new ArrayList<>(transfer.encode(ctx, wrapper.getProtocol()));
        List<DatagramPacket> datagramPackets = new ArrayList<>(byteBufs.size());
        for (ByteBuf byteBuf : byteBufs) {
            datagramPackets.add(new DatagramPacket(byteBuf, wrapper.getTargetAddress()));
        }

        return datagramPackets;
    }
}
