package org.kin.transport.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.kin.transport.netty.ObjectEncoder;
import org.kin.transport.netty.ServerObserver;
import org.kin.transport.netty.Session;
import org.kin.transport.netty.tcp.server.TcpServer;
import org.kin.transport.netty.tcp.server.TcpServerTransport;

import java.io.IOException;
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

    public static void main(String[] args) throws InterruptedException, IOException {
        TcpServer server = TcpServerTransport.create()
                .payloadProcessor((session, payload) -> {
                    String req = payload.data().toString(StandardCharsets.UTF_8);
                    System.out.println(req);
                    return session.sendObject(req, DEFAULT_ENCODER);
                })
                .channelInitializer(conn -> conn.addHandlerLast(new IdleStateHandler(5, 0, 0)))
                .observer(new ServerObserver<TcpServer>() {
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
                    public void onBound(TcpServer server) {
                        System.out.println("server bound!!!");
                    }

                    @Override
                    public void onClientConnected(TcpServer server, Session session) {
                        System.out.println("server accept client connect!!!");
                    }

                    @Override
                    public void onUnbound(TcpServer server) {
                        System.out.println("server unbound!!!");
                    }
                })
                .bind(10000);

        System.in.read();
//        Thread.currentThread().join();
        System.out.println("tcp server unbinding");
        server.dispose();
    }
}
