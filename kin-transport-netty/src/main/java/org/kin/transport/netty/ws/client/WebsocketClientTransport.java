package org.kin.transport.netty.ws.client;

import com.google.common.base.Preconditions;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.AbstractTransport;
import org.kin.transport.netty.ws.WebSocketConstants;
import reactor.netty.http.client.HttpClient;

import java.net.InetSocketAddress;

/**
 * 创建{@link  WebSocketClient}入口
 *
 * @author huangjianqin
 * @date 2023/1/19
 */
public final class WebsocketClientTransport extends AbstractTransport<WebsocketClientTransport> {
    /** 连接超时, 秒, 默认5s */
    private int connectTimeoutSec = 5;

    public static WebsocketClientTransport create() {
        return new WebsocketClientTransport();
    }

    private WebsocketClientTransport() {
    }

    /**
     * 构建websocket client实例
     */
    public WebSocketClient connect(int port) {
        return connect(new InetSocketAddress("0.0.0.0", port));
    }

    /**
     * 构建websocket client实例
     *
     * @param address remote的host:port并且handshake uri=/
     */
    public WebSocketClient connect(InetSocketAddress address) {
        String prefix = isSsl() ? WebSocketConstants.SSL_WS_PREFIX : WebSocketConstants.WS_PREFIX;
        return connect(prefix.concat(":/").concat(address.toString()));
    }

    /**
     * 构建websocket client实例
     *
     * @param uri handshake uri
     */
    public WebSocketClient connect(String uri) {
        check();
        Preconditions.checkArgument(StringUtils.isNotBlank(uri), "websocket handshake uri must be not blank");
        Preconditions.checkArgument(connectTimeoutSec > 0, "client connect timeout must be greater than 0");

        HttpClient httpClient = HttpClient.create();

        //要覆盖nettyHttpServer, 其方法返回的不是this, 是新实例
        if (isSsl()) {
            httpClient = httpClient.secure(this::secure);
        }

        httpClient = httpClient
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                // TODO: 2023/1/19 配置修改
                //打印底层event和二进制内容
//                .wiretap(false)
                //client允许接受压缩就开启压缩
                .compress(true)
                .keepAlive(true);

        return new WebSocketClient(this, httpClient, uri);
    }

    //setter && getter
    public int getConnectTimeoutSec() {
        return connectTimeoutSec;
    }

    public WebsocketClientTransport connectTimeoutSec(int connectTimeoutSec) {
        this.connectTimeoutSec = connectTimeoutSec;
        return this;
    }
}
