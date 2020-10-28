package org.kin.transport.netty.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.CompressionType;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.Transports;
import org.kin.transport.netty.socket.protocol.Protocol1;
import org.kin.transport.netty.socket.protocol.ProtocolFactory;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/31
 */
public class SocketTest {
    public static void main(String[] args) throws InterruptedException {
        ProtocolFactory.init("org.kin.transport");

        Server server = null;
        Client<SocketProtocol> client = null;
        try {
            InetSocketAddress address = new InetSocketAddress("0.0.0.0", 9000);
            client = Transports.socket().client().protocolHandler(new SocketProtocolHandler() {
                @Override
                public void handle(ChannelHandlerContext ctx, SocketProtocol protocol) {
                    System.out.println(protocol);
                }
            }).compress(CompressionType.FRAMED_LZ4).channelOption(ChannelOption.TCP_NODELAY, true).build().withReconnect(address);

            int count = 10;
            int i = 0;
            while (i++ < count) {
                System.out.println(i + "---------------------------------------");
                client.request(Protocol1.of(1));
                if (i % 3 == 0) {
                    if (Objects.isNull(server)) {
                        server = Transports.socket().server().protocolHandler(new SocketProtocolHandler() {
                            @Override
                            public void handle(ChannelHandlerContext ctx, SocketProtocol protocol) {
                                System.out.println(protocol);
                                ctx.channel().writeAndFlush(Protocol1.of(2));
                            }
                        }).compress(CompressionType.SNAPPY).channelOption(ChannelOption.TCP_NODELAY, true).build().bind(address);
                    } else {
                        server.close();
                        server = null;
                    }
                }
                System.out.println();
                Thread.sleep(5000);
            }
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
