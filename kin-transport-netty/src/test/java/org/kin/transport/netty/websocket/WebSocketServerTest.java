package org.kin.transport.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.kin.transport.netty.ObjectEncoder;
import org.kin.transport.netty.ServerObserver;
import org.kin.transport.netty.Session;
import org.kin.transport.netty.websocket.server.WebSocketServer;
import org.kin.transport.netty.websocket.server.WebSocketServerTransport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/19
 */
public class WebSocketServerTest {
    private static final ObjectEncoder<String> DEFAULT_ENCODER = (obj, outboundPayload) -> {
        ByteBuf byteBuf = outboundPayload.data();
        byteBuf.writeBytes(("hi " + obj).getBytes(StandardCharsets.UTF_8));
    };

    public static void main(String[] args) throws InterruptedException, IOException {
        WebSocketServer server = WebSocketServerTransport.create()
                .payloadProcessor((session, payload) -> {
                    String req = payload.data().toString(StandardCharsets.UTF_8);
                    System.out.println(req);

                    return session.sendObject(req, DEFAULT_ENCODER);
                })
                .channelInitializer(conn -> conn.addHandlerLast(new IdleStateHandler(5, 0, 0)))
                .observer(new ServerObserver<WebSocketServer>() {
                    @Override
                    public void onExceptionCaught(Session session, Throwable cause) {
                        System.out.println("server encounter exception!!!");
                    }

                    @Override
                    public void onIdle(Session session, IdleStateEvent event) {
                        System.out.println("server encounter idle event!!!");
                    }

                    @Override
                    public void onUserEventTriggered(Session session, Object event) {
                        System.out.println("server encounter user event!!!");
                    }

                    @Override
                    public void onBound(WebSocketServer server) {
                        System.out.println("server bound!!!");
                    }

                    @Override
                    public void onClientConnected(WebSocketServer server, Session session) {
                        System.out.println("server accept client connect!!!");
                    }

                    @Override
                    public void onUnbound(WebSocketServer server) {
                        System.out.println("server unbound!!!");
                    }
                })
                .bind(10000);

        System.in.read();
//        Thread.currentThread().join();
        System.out.println("websocket server unbinding");
        server.dispose();
    }
}
