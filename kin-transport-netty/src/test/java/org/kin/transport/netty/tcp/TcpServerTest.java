package org.kin.transport.netty.tcp;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.tcp.server.TcpServer;
import org.kin.transport.netty.tcp.server.TcpServerTransport;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public class TcpServerTest {
    public static void main(String[] args) throws InterruptedException {
        TcpServer server = TcpServerTransport.create()
                .payloadProcessor((session, payload) -> {
                    String req = payload.getData().toString(StandardCharsets.UTF_8);
                    System.out.println(req);

                    return session.write(op -> {
                        ByteBuf byteBuf = op.getData();
                        byteBuf.writeBytes(("hi " + req).getBytes(StandardCharsets.UTF_8));
                    });
                })
                .bind(10000);

        Thread.currentThread().join();
        System.out.println("tcp server closing");
        server.dispose();
    }
}
