package org.kin.transport.netty.ws;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.TimeUtils;
import org.kin.transport.netty.ws.client.WebSocketClient;
import org.kin.transport.netty.ws.client.WebsocketClientTransport;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/19
 */
public class WebSocketClientTest {
    public static void main(String[] args) throws InterruptedException {
        WebSocketClient client = WebsocketClientTransport.create()
                .payloadProcessor((session, payload) -> {
                    System.out.println(payload.getData().toString(StandardCharsets.UTF_8));
                    return Mono.empty();
                })
                .connect(10000);

        for (int i = 0; i < 1_000; i++) {
            int finalI = i;
            client.writeOrCache(outboundPayload -> {
                        System.out.println(TimeUtils.formatDateTime() + "::encoder: " + Thread.currentThread().getName() + "---" + finalI);
                        ByteBuf byteBuf = outboundPayload.getData();
                        byteBuf.writeBytes(("number: " + finalI).getBytes(StandardCharsets.UTF_8));
//                        byteBuf.writeBytes(StringUtils.randomString(100_000).getBytes(StandardCharsets.UTF_8));
                    })
                    .subscribe();
            Thread.sleep(1_000);
        }

//        Thread.currentThread().join();
        System.out.println("websocket client closing");
        client.dispose();
        System.exit(0);
    }
}
