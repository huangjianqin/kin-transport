package org.kin.transport.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.kin.transport.netty.ObjectEncoder;
import org.kin.transport.netty.tcp.server.TcpServer;
import org.kin.transport.netty.tcp.server.TcpServerTransport;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public class TcpServerTest {
    private static final ObjectEncoder<String> DEFAULT_ENCODER = (obj, outboundPayload) -> {
        ByteBuf byteBuf = outboundPayload.data();
        byteBuf.writeBytes(("hi " + obj).getBytes(StandardCharsets.UTF_8));
    };

    public static void main(String[] args) throws InterruptedException {
        TcpServer server = TcpServerTransport.create()
                .payloadProcessor((session, payload) -> {
                    try {
                        String req = payload.data().toString(StandardCharsets.UTF_8);
                        System.out.println(req);

                        return session.sendObject(req, DEFAULT_ENCODER);
                    } finally {
                        ReferenceCountUtil.safeRelease(payload);
                    }
                })
                .bind(10000);

        Thread.currentThread().join();
        System.out.println("tcp server closing");
        server.dispose();
    }
}
