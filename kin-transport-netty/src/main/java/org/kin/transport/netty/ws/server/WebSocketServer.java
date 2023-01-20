package org.kin.transport.netty.ws.server;

import io.netty.channel.ChannelHandler;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.handler.ServerHandler;
import org.kin.transport.netty.handler.WebSocketServerHandler;
import org.kin.transport.netty.ws.BinaryWebSocketFrameEncoder;
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
        PreHandlerInitializer preHandlerInitializer = serverTransport.getPreHandlerCustomizer();
        ProtocolOptions options = serverTransport.getProtocolOptions();
        LoopResources loopResources = LoopResources.create("kin-ws-server-" + port, 2, SysUtils.CPU_NUM * 2, false);
        httpServer.runOn(loopResources)
                .route(hsr -> hsr.ws(serverTransport.getHandshakeUrl(), (wsIn, wsOut) -> {
                            //channel共享handler
                            ProtocolEncoder protocolEncoder = new ProtocolEncoder(options);
                            wsIn.aggregateFrames()
                                    .withConnection(connection -> {
                                        List<ChannelHandler> preChannelHandlers = preHandlerInitializer.preHandlers(serverTransport);
                                        for (ChannelHandler preHandler : preChannelHandlers) {
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
                                                .addHandlerLast(ServerHandler.INSTANCE);
                                        onClientConnected(new Session(options, connection));
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
                    d.onDispose(() -> log().info("{}(port:{}) closed", serverName(), port));
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
