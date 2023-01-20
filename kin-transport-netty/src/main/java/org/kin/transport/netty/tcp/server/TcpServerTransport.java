package org.kin.transport.netty.tcp.server;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.kin.transport.netty.ProtocolTransport;

/**
 * 创建{@link TcpServer}的入口
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpServerTransport extends ProtocolTransport<TcpServerTransport> {

    public static TcpServerTransport create() {
        return new TcpServerTransport();
    }

    private TcpServerTransport() {
    }

    /**
     * 绑定tcp端口
     */
    public TcpServer bind(int port) {
        check();
        Preconditions.checkArgument(port > 0, "tcp server port must be greater than 0");

        //tcp
        reactor.netty.tcp.TcpServer tcpServer = reactor.netty.tcp.TcpServer.create();
        if (isSsl()) {
            tcpServer = tcpServer.secure(this::secure);
        }

        tcpServer = tcpServer.port(port)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                //打印底层event和二进制内容
//                .wiretap(false)
                .metrics(true);

        return new TcpServer(this, tcpServer, port);
    }
}
