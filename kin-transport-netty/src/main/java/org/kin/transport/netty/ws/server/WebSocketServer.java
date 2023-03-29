package org.kin.transport.netty.ws.server;

import io.netty.channel.ChannelHandler;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.tcp.handler.ServerHandler;
import org.kin.transport.netty.ws.BinaryWebSocketFrameEncoder;
import org.kin.transport.netty.ws.handler.WebSocketServerHandler;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.netty.resources.LoopResources;

import java.util.List;

/**
 * 基于websocket的{@link Server}实现
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
public final class WebSocketServer extends Server<WebsocketServerTransport> {
    WebSocketServer(WebsocketServerTransport serverTransport, HttpServer httpServer, int port) {
        super(serverTransport, port);

        onBind(serverTransport, httpServer);
    }

    /**
     * 监听端口
     */
    private void onBind(WebsocketServerTransport serverTransport, HttpServer httpServer) {
        //event loop
        LoopResources loopResources = LoopResources.create("kin-ws-server-" + port, 2, SysUtils.CPU_NUM * 2, false);

        ProtocolOptions options = serverTransport.getProtocolOptions();
        //前置handler
        List<ChannelHandler> preHandlers = serverTransport.getPreHandlers();

        ServerLifecycle lifecycle = serverTransport.getLifecycle();

        httpServer.runOn(loopResources)
                .route(hsr -> hsr.ws(serverTransport.getHandshakeUrl(), (wsIn, wsOut) -> {
                            //channel共享handler
                            ProtocolEncoder protocolEncoder = new ProtocolEncoder(options);
                            wsIn.aggregateFrames()
                                    .withConnection(connection -> {
                                        for (ChannelHandler preHandler : preHandlers) {
                                            connection.addHandlerLast(preHandler);
                                        }
                                        //核心handler
                                        connection
                                                //websocket额外handler
                                                .addHandlerLast(WebSocketServerHandler.INSTANCE)
                                                .addHandlerLast(BinaryWebSocketFrameEncoder.INSTANCE)
                                                //统一协议解析和处理
                                                .addHandlerLast(new ProtocolDecoder(options))
                                                .addHandlerLast(protocolEncoder)
                                                .addHandlerLast(new ServerHandler(lifecycle));
                                        Session session = new Session(options, connection);
                                        onClientConnected(session);

                                        lifecycle.onClientConnected(WebSocketServer.this, session);
                                    });
                            return Mono.never();
                        },
                        //websocket配置
                        WebsocketServerSpec.builder()
                                .compress(true)
                                .handlePing(true)
                                .maxFramePayloadLength(options.getMaxProtocolSize())
                                .build()))
                .doOnUnbound(d -> {
                    d.onDispose(loopResources);
                    d.onDispose(() -> lifecycle.onUnbound(WebSocketServer.this));
                    d.onDispose(() -> log().info("{}(port:{}) closed", serverName(), port));

                    lifecycle.onBound(WebSocketServer.this);
                })
                .bind()
                .cast(DisposableServer.class)
                //subscribe开始bind
                .subscribe(ds -> {
                    log().info("{} stated on port({})", serverName(), port);
                    disposable = ds;
                }, t -> log().error("{} encounter error when starting", serverName(), t));
    }
}
