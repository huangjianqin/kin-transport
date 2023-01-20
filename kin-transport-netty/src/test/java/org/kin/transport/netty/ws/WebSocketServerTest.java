package org.kin.transport.netty.ws;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.ws.server.WebSocketServer;
import org.kin.transport.netty.ws.server.WebsocketServerTransport;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/19
 */
public class WebSocketServerTest {
    public static void main(String[] args) throws InterruptedException {
        WebSocketServer server = WebsocketServerTransport.create()
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
        System.out.println("websocket server closing");
        server.dispose();
    }
}
