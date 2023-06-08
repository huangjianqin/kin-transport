package org.kin.transport.netty.tcp.client;

import org.kin.transport.netty.*;
import org.kin.transport.netty.handler.ClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;

import java.net.InetSocketAddress;

/**
 * 基于TCP的{@link Client}实现类
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpClient extends Client<TcpClient, TcpClientTransport> {
    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);

    /** remote address */
    private final InetSocketAddress address;
    /** tcp connect逻辑 */
    private final Mono<Connection> connector;

    TcpClient(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        super(clientTransport);
        this.address = address;
        this.connector = connect(clientTransport, tcpClient, address);
    }

    /**
     * tcp connect
     */
    private Mono<Connection> connect(TcpClientTransport clientTransport, reactor.netty.tcp.TcpClient tcpClient, InetSocketAddress address) {
        ProtocolOptions options = clientTransport.getProtocolOptions();

        ChannelInitializer channelInitializer = clientTransport.getChannelInitializer();

        //监听connection状态变化
        ConnectionObserver connectionObserver = (connection, newState) -> {
            if (!isDisposed() && newState == ConnectionObserver.State.DISCONNECTING) {
                log.info("channel closed, {}", connection.channel());
                connection.dispose();
            }
        };

        ClientObserver observer = clientTransport.getObserver();

        return tcpClient
                .observe(connectionObserver)
                .connect()
                .map(connection -> {
                    //注意, 下面逻辑不能在TcpClient.doOnConnected(...)进行, TcpClient.doOnConnected(...)的逻辑执行会比TcpClient.connect()后
                    log.info("{} connect to remote({}) success", clientName(), address);

                    channelInitializer.initChannel(connection);
                    //核心handler
                    connection.addHandlerLast(new ProtocolDecoder(options))
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
        return address.toString();
    }
}