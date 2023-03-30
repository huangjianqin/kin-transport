package org.kin.transport.netty.tcp.client;

import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.*;
import org.kin.transport.netty.tcp.handler.ClientHandler;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
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
    private final Mono<Connection> connector;

    TcpClient(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        super(clientTransport);
        this.address = address;
        this.connector = connect(clientTransport, tcpClient, address);

        tryReconnect();
    }

    /**
     * tcp connect
     */
    private Mono<Connection> connect(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        ProtocolOptions options = clientTransport.getProtocolOptions();

        //channel共享handler
        ProtocolEncoder protocolEncoder = new ProtocolEncoder(options);
        List<ChannelHandler> preHandlers = clientTransport.getPreHandlers();

        //监听connection状态变化
        ConnectionObserver connectionObserver = (connection, newState) -> {
            if (!isDisposed() && newState == ConnectionObserver.State.DISCONNECTING) {
                log().info("channel closed, {}", connection.channel());
                connection.dispose();
            }
        };

        ClientObserver observer = clientTransport.getObserver();

        return tcpClient
                .observe(connectionObserver)
                .connect()
                .map(connection -> {
                    //注意, 下面逻辑不能在TcpClient.doOnConnected(...)进行, TcpClient.doOnConnected(...)的逻辑执行会比TcpClient.connect()后
                    log().info("{} connect to remote({}) success", clientName(), address);
                    //pre handlers
                    for (ChannelHandler preHandler : preHandlers) {
                        connection.addHandlerLast(preHandler);
                    }
                    //核心handler
                    connection.addHandlerLast(new ProtocolDecoder(options))
                            .addHandlerLast(protocolEncoder)
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
        return address.toString();
    }
}