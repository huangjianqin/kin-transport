package org.kin.transport.netty.tcp.server;

import io.netty.channel.ChannelHandler;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.tcp.handler.ServerHandler;
import reactor.netty.DisposableServer;
import reactor.netty.resources.LoopResources;

import java.util.List;

/**
 * 基于TCP的{@link Server}实现类
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpServer extends Server<TcpServerTransport> {
    TcpServer(TcpServerTransport serverTransport, reactor.netty.tcp.TcpServer tcpServer, int port) {
        super(serverTransport, port);

        onBind(serverTransport, tcpServer);
    }

    /**
     * 监听端口
     */
    private void onBind(TcpServerTransport serverTransport, reactor.netty.tcp.TcpServer tcpServer) {
        //event loop
        LoopResources loopResources = LoopResources.create("kin-tcp-server-" + port, 2, SysUtils.DOUBLE_CPU, false);

        ProtocolOptions options = serverTransport.getProtocolOptions();
        //channel共享handler
        ProtocolEncoder protocolEncoder = new ProtocolEncoder(options);
        //前置handler
        List<ChannelHandler> preHandlers = serverTransport.getPreHandlers();

        ServerLifecycle lifecycle = serverTransport.getLifecycle();

        tcpServer.doOnConnection(connection -> {
                    //在channel init中add last handler会导致所添加的handler在名为"reactor.right.reactiveBridge"的ChannelOperationsHandler实例后面, 那么NettyInbound则是最原始的bytes
                    //NettyInbound.receiveObject() signal是ChannelOperationsHandler实例触发
                    //而Connection的addHandlerLast会保证ChannelOperationsHandler实例是pipeline最后一个handler
                    //pre handlers
                    for (ChannelHandler preHandler : preHandlers) {
                        connection.addHandlerLast(preHandler);
                    }
                    //核心handler
                    connection.addHandlerLast(new ProtocolDecoder(options))
                            .addHandlerLast(protocolEncoder)
                            .addHandlerLast(new ServerHandler(lifecycle));
                    Session session = new Session(options, connection);
                    onClientConnected(session);

                    lifecycle.onClientConnected(TcpServer.this, session);
                })
                .doOnBound(d -> {
                    //定义tcp server close逻辑
                    d.onDispose(loopResources);
                    d.onDispose(() -> lifecycle.onUnbound(TcpServer.this));
                    d.onDispose(() -> log().info("{}(port:{}) closed", serverName(), port));

                    lifecycle.onBound(TcpServer.this);
                })
                .bind()
                .cast(DisposableServer.class)
                //这里才subscribe, 真正启动tcp server
                .subscribe(ds -> {
                    log().info("{} started on port({})", serverName(), port);
                    disposable = ds;
                }, t -> log().error("{} encounter error when starting", serverName(), t));
    }
}
