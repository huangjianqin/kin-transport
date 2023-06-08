package org.kin.transport.netty.tcp.server;

import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.handler.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;

/**
 * 基于TCP的{@link Server}实现类
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpServer extends Server<TcpServer, TcpServerTransport> {
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);
    /** reactor netty tcp server */
    private final reactor.netty.tcp.TcpServer tcpServer;
    /** 标识server是否已调用{@link #bind()} */
    private volatile boolean bound;

    TcpServer(TcpServerTransport serverTransport, reactor.netty.tcp.TcpServer tcpServer, int port) {
        super(serverTransport, port);
        this.tcpServer = tcpServer;
    }

    @Override
    public TcpServer bind() {
        if (bound) {
            return this;
        }

        bound = true;
        onBind(serverTransport, tcpServer);
        return this;
    }

    /**
     * 监听端口
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void onBind(TcpServerTransport serverTransport, reactor.netty.tcp.TcpServer tcpServer) {
        //event loop
        LoopResources loopResources = LoopResources.create("kin-tcp-server-" + port, 2, SysUtils.DOUBLE_CPU, false);

        ProtocolOptions options = serverTransport.getProtocolOptions();
        ChannelInitializer channelInitializer = serverTransport.getChannelInitializer();

        ServerObserver observer = serverTransport.getObserver();

        tcpServer.doOnConnection(connection -> {
                    //在channel init中add last handler会导致所添加的handler在名为"reactor.right.reactiveBridge"的ChannelOperationsHandler实例后面, 那么NettyInbound则是最原始的bytes
                    //NettyInbound.receiveObject() signal是ChannelOperationsHandler实例触发
                    //而Connection的addHandlerLast会保证ChannelOperationsHandler实例是pipeline最后一个handler
                    channelInitializer.initChannel(connection);
                    //核心handler
                    connection.addHandlerLast(new ProtocolDecoder(options))
                            .addHandlerLast(new ProtocolEncoder(options))
                            .addHandlerLast(new ServerHandler(observer));
                    Session session = new Session(options, connection);
                    onClientConnected(session);

                    observer.onClientConnected(TcpServer.this, session);
                })
                .doOnBound(d -> {
                    //定义tcp server close逻辑
                    d.onDispose(loopResources);
                    d.onDispose(() -> observer.onUnbound(TcpServer.this));
                    d.onDispose(() -> log.info("{}(port:{}) closed", serverName(), port));

                    observer.onBound(TcpServer.this);
                })
                .bind()
                .cast(DisposableServer.class)
                //这里才subscribe, 真正启动tcp server
                .subscribe(ds -> {
                    log.info("{} started on port({})", serverName(), port);
                    onBound(ds);
                }, t -> log.error("{} encounter error when starting", serverName(), t));
    }
}
