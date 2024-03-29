package org.kin.transport.netty.websocket.server;

import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.handler.ServerHandler;
import org.kin.transport.netty.websocket.handler.BinaryWebSocketFrameEncoder;
import org.kin.transport.netty.websocket.handler.WebSocketFrameServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.WebsocketServerSpec;
import reactor.netty.resources.LoopResources;

/**
 * 基于websocket的{@link Server}实现
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
public final class WebSocketServer extends Server<WebSocketServer, WebSocketServerTransport> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    /** reactor netty http server */
    private final HttpServer httpServer;
    /** 标识server是否已执行{@link #bind()} */
    private volatile boolean bound;

    WebSocketServer(WebSocketServerTransport serverTransport, HttpServer httpServer, String host, int port) {
        super(serverTransport, host, port);
        this.httpServer = httpServer;
    }

    @Override
    public WebSocketServer bind() {
        if (bound) {
            return this;
        }

        bound = true;
        onBind(serverTransport, httpServer);
        return this;
    }

    /**
     * 监听端口
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void onBind(WebSocketServerTransport serverTransport, HttpServer httpServer) {
        //event loop
        LoopResources loopResources = LoopResources.create("kin-ws-server-" + port, 2, SysUtils.CPU_NUM * 2, false);

        ProtocolOptions options = serverTransport.getProtocolOptions();
        ChannelInitializer channelInitializer = serverTransport.getChannelInitializer();

        ServerObserver observer = serverTransport.getObserver();

        httpServer.runOn(loopResources)
                .route(hsr -> hsr.ws(serverTransport.getHandshakeUrl(), (wsIn, wsOut) -> {
                            wsIn.aggregateFrames()
                                    .withConnection(connection -> {
                                        channelInitializer.initChannel(connection);
                                        //核心handler
                                        connection
                                                //websocket额外handler
                                                .addHandlerLast(WebSocketFrameServerHandler.INSTANCE)
                                                .addHandlerLast(BinaryWebSocketFrameEncoder.INSTANCE)
                                                //统一协议解析和处理
                                                .addHandlerLast(new ProtocolDecoder(options))
                                                .addHandlerLast(new ProtocolEncoder(options))
                                                .addHandlerLast(new ServerHandler(observer));
                                        Session session = new Session(options, connection);
                                        onClientConnected(session);

                                        observer.onClientConnected(WebSocketServer.this, session);
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
                    d.onDispose(() -> observer.onUnbound(WebSocketServer.this));
                    d.onDispose(() -> log.info("{}({}:{}) closed", serverName(), host, port));

                    observer.onBound(WebSocketServer.this);
                })
                .bind()
                .cast(DisposableServer.class)
                //subscribe开始bind
                .subscribe(ds -> {
                    log.info("{} stated on {}:{}", serverName(), host, port);
                    onBound(ds);
                }, t -> log.error("{} encounter error when starting", serverName(), t));
    }
}
