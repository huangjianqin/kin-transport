package org.kin.transport.netty.udp.client;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.udp.UdpChannelHandlerInitializer;
import org.kin.transport.netty.udp.UdpProtocolDetails;
import org.kin.transport.netty.udp.UdpTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * udp client transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpClientTransportOption extends AbstractTransportOption<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpClientTransportOption> {
    /**
     * 构建udp client实例
     */
    public UdpClient connect(InetSocketAddress address) {
        ChannelHandlerInitializer<DatagramPacket, UdpProtocolDetails, DatagramPacket> channelHandlerInitializer =
                new UdpChannelHandlerInitializer<>(this);
        UdpClient client = new UdpClient(this, channelHandlerInitializer);
        client.connect(address);
        return client;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<DatagramPacket, UdpProtocolDetails, DatagramPacket> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new UdpTransfer(false);
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static UdpClientTransportOptionBuilder builder() {
        return new UdpClientTransportOptionBuilder();
    }

    public static class UdpClientTransportOptionBuilder
            extends TransportOptionBuilder<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpClientTransportOption, UdpClientTransportOptionBuilder> {
        public UdpClientTransportOptionBuilder() {
            super(new UdpClientTransportOption());
        }
    }
}
