package org.kin.transport.netty.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.SocketTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransportProtocolTransfer
        extends AbstractTransportProtocolTransfer<DatagramPacket, UdpProtocolWrapper, DatagramPacket> {
    private final SocketTransportProtocolTransfer transfer;

    public UdpTransportProtocolTransfer(boolean compression, boolean serverElseClient) {
        super(compression);
        this.transfer = new SocketTransportProtocolTransfer(compression, serverElseClient);
    }

    @Override
    public Collection<UdpProtocolWrapper> decode(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        Collection<AbstractSocketProtocol> protocols = transfer.decode(ctx, datagramPacket.content());
        return protocols.stream().map(UdpProtocolWrapper::new).collect(Collectors.toList());
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
