package org.kin.transport.netty.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.kin.transport.netty.Transports;
import org.kin.transport.netty.socket.protocol.Protocol1;
import org.kin.transport.netty.socket.protocol.ProtocolFactory;
import org.kin.transport.netty.udp.client.UdpClient;
import org.kin.transport.netty.udp.server.UdpServer;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTest {
    public static void main(String[] args) throws InterruptedException {
        ProtocolFactory.init("org.kin.transport");

        UdpServer server = null;
        UdpClient client = null;
        try {
            InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9000);
            server = Transports.datagram().server().protocolHandler(new UdpProtocolHandler() {
                @Override
                public void handle(ChannelHandlerContext ctx, UdpProtocolDetails details) {
                    System.out.println(details.getProtocol());
                    ctx.channel().writeAndFlush(UdpProtocolDetails.senderWrapper(Protocol1.of(2), details.getSenderAddress()));
                }
            }).channelOption(ChannelOption.TCP_NODELAY, true).build().bind(address);

            client = Transports.datagram().client().protocolHandler(new UdpProtocolHandler() {
                @Override
                public void handle(ChannelHandlerContext ctx, UdpProtocolDetails details) {
                    System.out.println(details.getProtocol());
                }
            }).channelOption(ChannelOption.TCP_NODELAY, true).build().connect(address);
            client.request(Protocol1.of(1));

            Thread.sleep(5000);
        } finally {
            if (Objects.nonNull(client)) {
                client.close();
            }
            if (Objects.nonNull(server)) {
                server.close();
            }
        }
    }
}
