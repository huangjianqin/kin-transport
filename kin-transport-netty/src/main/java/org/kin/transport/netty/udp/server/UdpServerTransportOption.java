package org.kin.transport.netty.udp.server;

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
 * udp server transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpServerTransportOption extends AbstractTransportOption<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpServerTransportOption> {
    /**
     * 构建udp server实例
     */
    public UdpServer bind(InetSocketAddress address) {
        ChannelHandlerInitializer<DatagramPacket, UdpProtocolDetails, DatagramPacket>
                channelHandlerInitializer = new UdpChannelHandlerInitializer<>(this);
        UdpServer server = new UdpServer(this, channelHandlerInitializer);
        server.bind(address);
        return server;
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<DatagramPacket, UdpProtocolDetails, DatagramPacket> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new UdpTransfer(true);
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static UdpServerTransportOptionBuilder builder() {
        return new UdpServerTransportOptionBuilder();
    }

    public static class UdpServerTransportOptionBuilder
            extends TransportOptionBuilder<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpServerTransportOption, UdpServerTransportOptionBuilder> {
        public UdpServerTransportOptionBuilder() {
            super(new UdpServerTransportOption());
        }
    }
}
