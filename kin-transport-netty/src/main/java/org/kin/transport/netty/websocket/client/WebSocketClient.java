package org.kin.transport.netty.websocket.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.transport.netty.*;
import org.kin.transport.netty.handler.ClientHandler;
import org.kin.transport.netty.websocket.handler.BinaryWebSocketFrameEncoder;
import org.kin.transport.netty.websocket.handler.WebSocketFrameClientHandler;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;

import java.util.List;

/**
 * 基于websocket的{@link Client}实现类
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
public final class WebSocketClient extends Client<WebSocketClientTransport> {
    /** handshake uri */
    private final String uri;
    /** websocket connect逻辑 */
    private final Mono<Connection> connector;

    WebSocketClient(WebSocketClientTransport clientTransport, HttpClient httpClient, String uri) {
        super(clientTransport);
        this.uri = uri;
        this.connector = connect(clientTransport, httpClient, uri);

        tryReconnect();
    }

    /**
     * websocket connect
     */
    private Mono<Connection> connect(WebSocketClientTransport clientTransport, HttpClient httpClient, String uri) {
        ProtocolOptions options = clientTransport.getProtocolOptions();

        List<ChannelHandler> preHandlers = clientTransport.getPreHandlers();

        //监听connection状态变化
        ConnectionObserver connectionObserver = (connection, newState) -> {
            if (!isDisposed() && newState == ConnectionObserver.State.DISCONNECTING) {
                log().info("channel closed, {}", connection.channel());
                connection.dispose();
            }
        };

        ClientObserver observer = clientTransport.getObserver();

        return httpClient
                .observe(connectionObserver)
                .websocket(WebsocketClientSpec.builder()
                        .version(WebSocketVersion.V13)
                        .compress(true)
                        .handlePing(true)
                        .maxFramePayloadLength(options.getMaxProtocolSize() + options.getHeaderSize())
                        .build())
                .uri(uri)
                .connect()
                .map(connection -> {
                    log().info("{} connect to remote({}) success", clientName(), uri);
                    //pre handlers
                    for (ChannelHandler preHandler : preHandlers) {
                        connection.addHandlerLast(preHandler);
                    }
                    //核心handler
                    connection
                            //websocket额外handler
                            .addHandlerLast(WebSocketFrameClientHandler.INSTANCE)
                            .addHandlerLast(BinaryWebSocketFrameEncoder.INSTANCE)
                            //统一协议解析和处理
                            .addHandlerLast(new ProtocolDecoder(options))
                            .addHandlerLast(new ProtocolEncoder(options))
                            .addHandlerLast(new ClientHandler(observer));
                    return connection;
                });
    }

    @Override
    protected Mono<Connection> connector() {
        return connector;
    }

    @Override
    protected String remoteAddress() {
        return uri;
    }
}
