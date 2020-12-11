package org.kin.transport.netty.udp;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * udp server transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransportOption extends AbstractTransportOption<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpTransportOption> {
    /** 标识tcp server还是tcp client */
    private final boolean serverElseClient;

    private UdpTransportOption(boolean serverElseClient) {
        this.serverElseClient = serverElseClient;
    }

    /**
     * 构建udp server实例
     */
    public UdpServer bind(InetSocketAddress address) {
        if (!serverElseClient) {
            throw new UnsupportedOperationException("this is a tpc client transport options");
        }
        ChannelHandlerInitializer<DatagramPacket, UdpProtocolDetails, DatagramPacket>
                channelHandlerInitializer = new UdpChannelHandlerInitializer<>(this);
        UdpServer server = new UdpServer(this, channelHandlerInitializer);
        server.bind(address);
        return server;
    }

    /**
     * 构建udp client实例
     */
    public UdpClient connect(InetSocketAddress address) {
        if (serverElseClient) {
            throw new UnsupportedOperationException("this is a tpc server transport options");
        }
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
                new UdpTransfer(serverElseClient);
    }

    //------------------------------------------------------builder------------------------------------------------------

    /**
     * udp server配置
     */
    public static UdpServerTransportOptionBuilder server() {
        return new UdpServerTransportOptionBuilder(true);
    }

    /**
     * udp client配置
     */
    public static UdpServerTransportOptionBuilder client() {
        return new UdpServerTransportOptionBuilder(false);
    }

    public static class UdpServerTransportOptionBuilder
            extends TransportOptionBuilder<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpTransportOption, UdpServerTransportOptionBuilder> {
        public UdpServerTransportOptionBuilder(boolean serverElseClient) {
            super(new UdpTransportOption(serverElseClient));
        }
    }
}
