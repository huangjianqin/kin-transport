package org.kin.transport.netty.udp.client;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.UdpClient;
import org.kin.transport.netty.udp.AbstractUdpTransportOption;
import org.kin.transport.netty.udp.UdpChannelHandlerInitializer;
import org.kin.transport.netty.udp.UdpProtocolWrapper;
import org.kin.transport.netty.udp.UdpTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * udp client transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpClientTransportOption extends AbstractUdpTransportOption<UdpClientTransportOption> {
    /**
     * 构建udp client实例
     */
    public UdpClient build(InetSocketAddress address) {
        ChannelHandlerInitializer<DatagramPacket, UdpProtocolWrapper, DatagramPacket> channelHandlerInitializer =
                new UdpChannelHandlerInitializer<>(this);
        UdpClient client = new UdpClient(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public TransportProtocolTransfer<DatagramPacket, UdpProtocolWrapper, DatagramPacket> getTransportProtocolTransfer() {
        return Objects.nonNull(super.getTransportProtocolTransfer()) ?
                super.getTransportProtocolTransfer() :
                //默认
                new UdpTransfer(isCompression(), false, getGlobalRateLimit());
    }
}
