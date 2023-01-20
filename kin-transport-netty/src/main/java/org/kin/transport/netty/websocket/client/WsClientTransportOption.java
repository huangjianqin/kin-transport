package org.kin.transport.netty.websocket.client;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.*;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.WsClientOptionOprs;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;
import org.kin.transport.netty.ws.WebSocketConstants;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * websocket client 传输配置
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsClientTransportOption<MSG, INOUT extends WebSocketFrame>
        extends AbstractWsTransportOption<MSG, INOUT, WsClientTransportOption<MSG, INOUT>>
        implements WsClientOptionOprs<Client<MSG>>, LoggerOprs {
    /** 握手等待时间(毫秒), 支持重连时, 最好设置, 不然会一直阻塞 */
    private long handshakeTimeout;

    /**
     * 构建websocket client实例
     */
    @Override
    public final Client<MSG> connect(InetSocketAddress address) {
        return connect(address, false);
    }

    /**
     * 构建websocket client实例
     */
    @Override
    public final Client<MSG> connect(String url) {
        return connect(url);
    }

    /**
     * 构建websocket client实例
     *
     * @param reconnect 重连标识
     */
    private final Client<MSG> connect(InetSocketAddress address, boolean reconnect) {
        String prefix = isSsl() ? WebSocketConstants.SSL_WS_PREFIX : WebSocketConstants.WS_PREFIX;
        return connect(prefix.concat(":/").concat(address.toString()).concat(getHandshakeUrl()), reconnect);
    }

    /**
     * 构建websocket client实例
     */
    private final Client<MSG> connect(String url, boolean reconnect) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("invalid url '%s'", url), e);
        }
        String scheme = uri.getScheme() == null ? WebSocketConstants.WS_PREFIX : uri.getScheme();
        String host = uri.getHost() == null ? NetUtils.getLocalAddress().toString() : uri.getHost();
        int port;
        if (uri.getPort() == -1) {
            if (WebSocketConstants.WS_PREFIX.equalsIgnoreCase(scheme)) {
                port = 80;
            } else if (WebSocketConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
                throw new IllegalArgumentException(String.format("invlid port '%d'", port));
            }
        } else {
            port = uri.getPort();
        }

        if (!WebSocketConstants.WS_PREFIX.equalsIgnoreCase(scheme) && !WebSocketConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(String.format("Only ws(s) is supported. now is '%s'", scheme));
        }

        boolean ssl = WebSocketConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme);
        if (ssl && !isSsl()) {
            throw new IllegalArgumentException("transport config not open ssl");
        }

        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        //websocker frame处理
        WsClientHandler wsClientHandler = new WsClientHandler(handshaker);

        WsClientHandlerInitializer<MSG, INOUT> handlerInitializer = new WsClientHandlerInitializer<>(this, wsClientHandler);
        Client<MSG> client = new Client<>(this, handlerInitializer);
        client.connect(new InetSocketAddress(host, port));

        try {
            //阻塞等待是否握手成功
            wsClientHandler.handshakeFuture().await(handshakeTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {

        } catch (Exception e) {
            if (reconnect) {
                log().error("", e);
            } else {
                //不是重连才报错
                ExceptionUtils.throwExt(e);
            }
        }

        if (!wsClientHandler.handshakeFuture().isDone() && !reconnect) {
            //不是重连才报错
            ExceptionUtils.throwExt(wsClientHandler.handshakeFuture().cause());
        }

        return client;
    }

    /**
     * 构建支持自动重连的websocket client实例
     */
    @Override
    public final Client<MSG> withReconnect(InetSocketAddress address) {
        return withReconnect(address, true);
    }

    /**
     * 构建支持自动重连的websocket client实例
     *
     * @param cacheMessage 是否缓存断开链接时发送的消息
     */
    @Override
    public final Client<MSG> withReconnect(InetSocketAddress address, boolean cacheMessage) {
        if (handshakeTimeout <= 0) {
            //支持重连时, 没有设置, 默认2s握手超时
            handshakeTimeout = 2000;
        }
        ReconnectClient<MSG> client = new ReconnectClient<>(this, new ReconnectTransportOption<MSG>() {
            @Override
            public Client<MSG> reconnect(InetSocketAddress address) {
                return connect(address, true);
            }

            @Override
            public void wrapProtocolHandler(ProtocolHandler<MSG> protocolHandler) {
                WsClientTransportOption.super.protocolHandler = protocolHandler;
            }
        }, cacheMessage);
        client.connect(address);
        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<INOUT, MSG, INOUT> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            //默认
            return getDefaultTransportProtocolTransfer(false);
        }

        return super.getTransportProtocolTransfer();
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static <MSG, INOUT extends WebSocketFrame> WsClientTransportOptionBuilder<MSG, INOUT> builder() {
        return new WsClientTransportOptionBuilder<>();
    }

    public static class WsClientTransportOptionBuilder<MSG, INOUT extends WebSocketFrame>
            extends WsTransportOptionBuilder<MSG, INOUT, WsClientTransportOption<MSG, INOUT>, WsClientTransportOptionBuilder<MSG, INOUT>>
            implements WsClientOptionOprs<Client<MSG>> {
        public WsClientTransportOptionBuilder() {
            super(new WsClientTransportOption<>());
        }

        public WsClientTransportOptionBuilder<MSG, INOUT> handshakeTimeout(long handshakeTimeout) {
            transportOption.handshakeTimeout = handshakeTimeout;
            return this;
        }

        @Override
        public Client<MSG> connect(InetSocketAddress address) {
            return build().connect(address);
        }

        @Override
        public Client<MSG> withReconnect(InetSocketAddress address) {
            return withReconnect(address, true);
        }

        @Override
        public Client<MSG> withReconnect(InetSocketAddress address, boolean cacheMessage) {
            return build().withReconnect(address, cacheMessage);
        }

        @Override
        public Client<MSG> connect(String url) {
            return build().connect(url);
        }
    }
}
