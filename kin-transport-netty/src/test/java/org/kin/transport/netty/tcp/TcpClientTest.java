package org.kin.transport.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.kin.framework.utils.TimeUtils;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.ClientLifecycle;
import org.kin.transport.netty.ObjectEncoder;
import org.kin.transport.netty.Session;
import org.kin.transport.netty.tcp.client.TcpClient;
import org.kin.transport.netty.tcp.client.TcpClientTransport;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public class TcpClientTest {
    private static final ObjectEncoder<Integer> DEFAULT_ENCODER = (obj, outboundPayload) -> {
        System.out.println(TimeUtils.formatDateTime() + "::encoder: " + Thread.currentThread().getName() + "---" + obj);
        ByteBuf byteBuf = outboundPayload.data();
        byteBuf.writeBytes(("number: " + obj).getBytes(StandardCharsets.UTF_8));
//                        byteBuf.writeBytes(StringUtils.randomString(100_000).getBytes(StandardCharsets.UTF_8));
    };

    public static void main(String[] args) throws InterruptedException {
        TcpClient client = TcpClientTransport.create()
                .payloadProcessor((session, payload) -> {
                    System.out.println(payload.data().toString(StandardCharsets.UTF_8));
                    return Mono.empty();
                })
                .addHandlers(new IdleStateHandler(5, 0, 0))
                .lifecycle(new ClientLifecycle() {
                    @Override
                    public void onExceptionCaught(Session session, Throwable cause) {
                        System.out.println("client encounter exception!!!");
                    }

                    @Override
                    public void onIdle(Session session, IdleStateEvent event) {
                        System.out.println("client encounter idle event!!!");
                    }

                    @Override
                    public void onUserEventTriggered(Session session, Object event) {
                        System.out.println("client encounter user event!!!");
                    }

                    @Override
                    public <C extends Client<?>> void onConnected(C client, Session session) {
                        System.out.println("client connected!!!");
                    }

                    @Override
                    public <C extends Client<?>> void onReconnected(C client, Session session) {
                        System.out.println("client reconnected!!!");
                    }
                })
                .connect(10000);

        for (int i = 0; i < 1_000; i++) {
            client.sendObject(i, DEFAULT_ENCODER)
                    .subscribe();
            Thread.sleep(1_000);
        }

//        Thread.currentThread().join();
        System.out.println("tcp client closing");
        client.dispose();
        System.exit(0);
    }
}
