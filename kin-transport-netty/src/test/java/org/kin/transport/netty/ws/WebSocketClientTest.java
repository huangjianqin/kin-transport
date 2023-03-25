package org.kin.transport.netty.ws;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.TimeUtils;
import org.kin.transport.netty.ObjectEncoder;
import org.kin.transport.netty.ws.client.WebSocketClient;
import org.kin.transport.netty.ws.client.WebsocketClientTransport;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/19
 */
public class WebSocketClientTest {
    private static final ObjectEncoder<Integer> DEFAULT_ENCODER = (obj, outboundPayload) -> {
        System.out.println(TimeUtils.formatDateTime() + "::encoder: " + Thread.currentThread().getName() + "---" + obj);
        ByteBuf byteBuf = outboundPayload.data();
        byteBuf.writeBytes(("number: " + obj).getBytes(StandardCharsets.UTF_8));
//                        byteBuf.writeBytes(StringUtils.randomString(100_000).getBytes(StandardCharsets.UTF_8));
    };

    public static void main(String[] args) throws InterruptedException {
        WebSocketClient client = WebsocketClientTransport.create()
                .payloadProcessor((session, payload) -> {
                    System.out.println(payload.data().toString(StandardCharsets.UTF_8));
                    return Mono.empty();
                })
                .connect(10000);

        for (int i = 0; i < 1_000; i++) {
            client.sendObject(i, DEFAULT_ENCODER)
                    .subscribe();
            Thread.sleep(1_000);
        }

//        Thread.currentThread().join();
        System.out.println("websocket client closing");
        client.dispose();
        System.exit(0);
    }
}
