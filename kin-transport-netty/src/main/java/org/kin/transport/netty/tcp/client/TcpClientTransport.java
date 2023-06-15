package org.kin.transport.netty.tcp.client;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.util.NetUtil;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.AdvancedClientTransport;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;

/**
 * 创建{@link org.kin.transport.netty.tcp.client.TcpClient}入口
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class TcpClientTransport extends AdvancedClientTransport<TcpClientTransport> {
    /** client端统一loop resource, 没必要每次create都创建新的 */
    private static final LoopResources LOOP_RESOURCES = LoopResources.create("kin-tcp-client", SysUtils.getIntSysProperty("kin.transport.tcp.worker", SysUtils.CPU_NUM), false);

    static {
        JvmCloseCleaner.instance().add(LOOP_RESOURCES::dispose);
    }

    /** 连接超时, 秒, 默认5s */
    private int connectTimeoutSec = 5;

    public static TcpClientTransport create() {
        return new TcpClientTransport();
    }

    private TcpClientTransport() {
    }

    /**
     * create
     */
    public org.kin.transport.netty.tcp.client.TcpClient create(int port) {
        return create(NetUtil.LOCALHOST.getHostAddress(), port);
    }

    /**
     * create
     */
    public org.kin.transport.netty.tcp.client.TcpClient create(String host, int port) {
        return create(new InetSocketAddress(host, port));
    }

    /**
     * create
     */
    public org.kin.transport.netty.tcp.client.TcpClient create(InetSocketAddress address) {
        check();
        Preconditions.checkArgument(connectTimeoutSec > 0, "client connect timeout must be greater than 0");

        //tcp
        TcpClient tcpClient = TcpClient.create();
        if (isSsl()) {
            tcpClient = tcpClient.secure(this::secure);
        }

        tcpClient = tcpClient
                .remoteAddress(() -> address)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeoutSec() * 1000)
                //打印底层event和二进制内容
//                .wiretap(false)
                .metrics(true)
                .runOn(LOOP_RESOURCES);

        tcpClient = applyOptions(tcpClient);

        return new org.kin.transport.netty.tcp.client.TcpClient(this, tcpClient, address);
    }

    /**
     * tcp connect
     */
    public org.kin.transport.netty.tcp.client.TcpClient connect(int port) {
        return create(NetUtil.LOCALHOST.getHostAddress(), port).connect();
    }

    /**
     * tcp connect
     */
    public org.kin.transport.netty.tcp.client.TcpClient connect(String host, int port) {
        return create(new InetSocketAddress(host, port)).connect();
    }

    /**
     * tcp connect
     */
    public org.kin.transport.netty.tcp.client.TcpClient connect(InetSocketAddress address) {
        return create(address).connect();
    }

    //getter
    public int getConnectTimeoutSec() {
        return connectTimeoutSec;
    }

    public TcpClientTransport connectTimeoutSec(int connectTimeoutSec) {
        this.connectTimeoutSec = connectTimeoutSec;
        return this;
    }
}
