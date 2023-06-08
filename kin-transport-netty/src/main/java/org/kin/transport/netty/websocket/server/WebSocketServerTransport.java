package org.kin.transport.netty.websocket.server;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.util.NetUtil;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.ProtocolServerTransport;
import org.kin.transport.netty.websocket.WebSocketConstants;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

/**
 * 创建{@link WebSocketServer}入口
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
public final class WebSocketServerTransport extends ProtocolServerTransport<WebSocketServerTransport> {
    /** websocket 握手url */
    private String handshakeUrl = WebSocketConstants.WS_PATH;

    public static WebSocketServerTransport create() {
        return new WebSocketServerTransport();
    }

    private WebSocketServerTransport() {
    }

    /**
     * 监听端口
     */
    public WebSocketServer bind() {
        return bind(8080);
    }

    /**
     * 监听端口, 以http1.1握手
     */
    public WebSocketServer bind(int port) {
        return bind(NetUtil.LOCALHOST.getHostAddress(), port, HttpProtocol.HTTP11);
    }

    /**
     * 监听端口, 以http2握手
     */
    public WebSocketServer bind2(int port) {
        return bind(NetUtil.LOCALHOST.getHostAddress(), port, HttpProtocol.H2);
    }

    /**
     * 监听端口
     */
    public WebSocketServer bind(String host) {
        return bind(host, 8080);
    }

    /**
     * 监听端口, 以http1.1握手
     */
    public WebSocketServer bind(String host, int port) {
        return bind(host, port, HttpProtocol.HTTP11);
    }

    /**
     * 监听端口, 以http2握手
     */
    public WebSocketServer bind2(String host, int port) {
        return bind(host, port, HttpProtocol.H2);
    }

    /**
     * 监听端口
     */
    public WebSocketServer bind(String host, int port, HttpProtocol protocol) {
        check();
        Preconditions.checkArgument(port > 0, "websocket server port must be greater than 0");
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "websocket server host must be not blank");

        HttpServer httpServer = HttpServer.create();

        //要覆盖nettyHttpServer, 其方法返回的不是this, 是新实例
        if (isSsl()) {
            httpServer = httpServer.secure(this::secure);
        }

        httpServer = httpServer.host(host).port(port)
                .protocol(protocol)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //打印底层event和二进制内容
//                .wiretap(false)
                .accessLog(true)
                //>=256KB+client允许接受压缩就开启压缩
                .compress(true)
                .compress(256 * 1024)
                //30sec空闲超时
                .idleTimeout(Duration.ofSeconds(30))
                //最多最存在512个待处理的http request
                .maxKeepAliveRequests(16);

        httpServer = applyChildOptions(httpServer);
        httpServer = applyOptions(httpServer);

        return new WebSocketServer(this, httpServer, port);
    }

    //getter
    public String getHandshakeUrl() {
        return handshakeUrl;
    }

    public WebSocketServerTransport handshakeUrl(String handshakeUrl) {
        this.handshakeUrl = handshakeUrl;
        return this;
    }
}