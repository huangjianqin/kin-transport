package org.kin.transport.netty.tcp.client;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.common.*;
import reactor.core.publisher.Mono;
import reactor.netty.ConnectionObserver;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于TCP的{@link Client}实现类
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpClient extends Client<TcpClientTransport> {
    /** remote address */
    private final InetSocketAddress address;
    /** tcp connect逻辑 */
    private final Mono<Session> connector;

    TcpClient(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        super(clientTransport);
        this.address = address;
        this.connector = connect(clientTransport, tcpClient, address);

        tryReconnect();
    }

    /**
     * tcp connect
     */
    private Mono<Session> connect(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        ProtocolOptions options = clientTransport.getProtocolOptions();

        //channel共享handler
        ProtocolEncoder protocolEncoder = new ProtocolEncoder(options);
        PreHandlerInitializer preHandlerInitializer = clientTransport.getPreHandlerCustomizer();

        //监听connection状态变化
        ConnectionObserver connectionObserver = (connection, newState) -> {
            if (!isDisposed() && newState == ConnectionObserver.State.DISCONNECTING) {
                log().info("channel closed, {}", connection.channel());
                connection.dispose();
            }
        };

        return tcpClient
                .observe(connectionObserver)
                .connect()
                .map(connection -> {
                    //注意, 下面逻辑不能在TcpClient.doOnConnected(...)进行, TcpClient.doOnConnected(...)的逻辑执行会比TcpClient.connect()后
                    log().info("{} connect to remote({}) success", clientName(), address);
                    //pre handlers
                    List<ChannelHandler> preChannelHandlers = preHandlerInitializer.preHandlers(clientTransport);
                    for (ChannelHandler preHandler : preChannelHandlers) {
                        connection.addHandlerLast(preHandler);
                    }
                    //核心handler
                    connection.addHandlerLast(new ProtocolDecoder(options))
                            .addHandlerLast(protocolEncoder)
                            .addHandlerLast(ClientHandler.INSTANCE);

                    Session session = new Session(options, connection);

                    onConnected(session);
                    return session;
                });
    }

    @Override
    protected Mono<Session> connector() {
        return connector;
    }

    @Override
    protected String remoteDesc() {
        return address.toString();
    }
}