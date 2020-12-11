package org.kin.transport.netty.udp;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.*;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * udp server transport配置
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransportOption extends AbstractTransportOption<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpTransportOption>
        implements ServerOptionOprs<UdpServer>, ClientOptionOprs<UdpClient> {
    /** 标识tcp server还是tcp client */
    private final boolean serverElseClient;

    private UdpTransportOption(boolean serverElseClient) {
        this.serverElseClient = serverElseClient;
    }

    /**
     * 构建udp server实例
     */
    @Override
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
    @Override
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
     * 通用builder
     */
    private static class UdpTransportOptionBuilder<B extends UdpTransportOptionBuilder<B>>
            extends TransportOptionBuilder<DatagramPacket, UdpProtocolDetails, DatagramPacket, UdpTransportOption, B> {
        private UdpTransportOptionBuilder(boolean serverElseClient) {
            super(new UdpTransportOption(serverElseClient));
        }
    }

    /**
     * server builder
     */
    public static class UdpServerTransportOptionBuilder extends UdpTransportOptionBuilder<UdpServerTransportOptionBuilder> implements ServerOptionOprs<UdpServer> {

        public UdpServerTransportOptionBuilder() {
            super(true);
        }

        @Override
        public UdpServer bind(InetSocketAddress address) {
            return build().bind(address);
        }
    }

    /**
     * client builder
     */
    public static class UdpClientTransportOptionBuilder extends UdpTransportOptionBuilder<UdpClientTransportOptionBuilder> implements ClientOptionOprs<UdpClient> {

        public UdpClientTransportOptionBuilder() {
            super(false);
        }

        @Override
        public UdpClient connect(InetSocketAddress address) {
            return build().connect(address);
        }
    }
}
