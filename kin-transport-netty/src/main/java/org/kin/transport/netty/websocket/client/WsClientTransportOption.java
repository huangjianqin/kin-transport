package org.kin.transport.netty.websocket.client;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.WsConstants;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * websocket client 传输配置
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsClientTransportOption<MSG, INOUT extends WebSocketFrame>
        extends AbstractWsTransportOption<MSG, INOUT, WsClientTransportOption<MSG, INOUT>> {
    /**
     * 构建websocket client实例
     */
    public final Client<MSG> connect(InetSocketAddress address) {
        String prefix = isSsl() ? WsConstants.SSL_WS_PREFIX : WsConstants.WS_PREFIX;
        return connect(prefix.concat(":/").concat(address.toString()).concat(getHandshakeUrl()));
    }

    /**
     * 构建websocket client实例
     */
    public final Client<MSG> connect(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("invalid url '%s'", url), e);
        }
        String scheme = uri.getScheme() == null ? WsConstants.WS_PREFIX : uri.getScheme();
        String host = uri.getHost() == null ? NetUtils.getLocalAddress().toString() : uri.getHost();
        int port;
        if (uri.getPort() == -1) {
            if (WsConstants.WS_PREFIX.equalsIgnoreCase(scheme)) {
                port = 80;
            } else if (WsConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
                throw new IllegalArgumentException(String.format("invlid port '%d'", port));
            }
        } else {
            port = uri.getPort();
        }

        if (!WsConstants.WS_PREFIX.equalsIgnoreCase(scheme) && !WsConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(String.format("Only ws(s) is supported. now is '%s'", scheme));
        }

        boolean ssl = WsConstants.SSL_WS_PREFIX.equalsIgnoreCase(scheme);
        if (ssl && !isSsl()) {
            throw new IllegalArgumentException("transport config not open ssl");
        }

        //进行握手
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        //websocker frame处理
        WsClientHandler wsClientHandler = new WsClientHandler(handshaker);

        WsClientHandlerInitializer<MSG, INOUT> handlerInitializer = new WsClientHandlerInitializer<>(this, wsClientHandler);
        Client<MSG> client = new Client<>(new InetSocketAddress(host, port));
        client.connect(this, handlerInitializer);

        try {
            //阻塞等待是否握手成功
            wsClientHandler.handshakeFuture().sync();
        } catch (InterruptedException e) {

        }

        if (!wsClientHandler.handshakeFuture().isDone()) {
            throw new RuntimeException(wsClientHandler.handshakeFuture().cause());
        }

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
            extends WsTransportOptionBuilder<MSG, INOUT, WsClientTransportOption<MSG, INOUT>> {
        public WsClientTransportOptionBuilder() {
            super(new WsClientTransportOption<>());
        }
    }
}
