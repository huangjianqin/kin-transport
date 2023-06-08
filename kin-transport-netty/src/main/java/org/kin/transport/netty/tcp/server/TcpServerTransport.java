package org.kin.transport.netty.tcp.server;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.util.NetUtil;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.ProtocolServerTransport;

/**
 * 创建{@link TcpServer}的入口
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpServerTransport extends ProtocolServerTransport<TcpServerTransport> {

    public static TcpServerTransport create() {
        return new TcpServerTransport();
    }

    private TcpServerTransport() {
    }

    /**
     * create
     */
    public TcpServer create(int port) {
        return create(NetUtil.LOCALHOST.getHostAddress(), port);
    }

    /**
     * create
     */
    public TcpServer create(String host, int port) {
        check();
        Preconditions.checkArgument(port > 0, "tcp server port must be greater than 0");
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "tcp server host must be not blank");

        //tcp
        reactor.netty.tcp.TcpServer tcpServer = reactor.netty.tcp.TcpServer.create();
        if (isSsl()) {
            tcpServer = tcpServer.secure(this::secure);
        }

        tcpServer = tcpServer.host(host).port(port)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                //打印底层event和二进制内容
//                .wiretap(false)
                .metrics(true);

        tcpServer = applyOptions(tcpServer);
        tcpServer = applyChildOptions(tcpServer);

        return new TcpServer(this, tcpServer, host, port);
    }

    /**
     * listen
     */
    public TcpServer bind(int port) {
        return create(NetUtil.LOCALHOST.getHostAddress(), port).bind();
    }

    /**
     * listen
     */
    public TcpServer bind(String host, int port) {
        return create(host, port).bind();
    }
}
