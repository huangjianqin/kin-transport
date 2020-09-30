package org.kin.transport.netty.udp.server;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.udp.UdpChannelHandlerInitializer;
import org.kin.transport.netty.udp.UdpProtocolWrapper;
import org.kin.transport.netty.udp.UdpTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * udp server transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpServerTransportOption extends AbstractTransportOption<DatagramPacket, UdpProtocolWrapper, DatagramPacket, UdpServerTransportOption> {
    /**
     * 构建udp server实例
     */
    public UdpServer bind(InetSocketAddress address) {
        ChannelHandlerInitializer<DatagramPacket, UdpProtocolWrapper, DatagramPacket>
                channelHandlerInitializer = new UdpChannelHandlerInitializer<>(this);
        UdpServer server = new UdpServer(address);
        server.bind(this, channelHandlerInitializer);
        return server;
    }

    //------------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<DatagramPacket, UdpProtocolWrapper, DatagramPacket> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new UdpTransfer(true);
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static UdpServerTransportOptionBuilder builder() {
        return new UdpServerTransportOptionBuilder();
    }

    public static class UdpServerTransportOptionBuilder extends TransportOptionBuilder<DatagramPacket, UdpProtocolWrapper, DatagramPacket, UdpServerTransportOption> {
        public UdpServerTransportOptionBuilder() {
            super(new UdpServerTransportOption());
        }
    }
}
