package org.kin.transport.netty.websocket.client;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsClientTransportOption extends AbstractWsTransportOption {
    public ProtocolBaseWsClientTransportOption protocol() {
        return new ProtocolBaseWsClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------

    public final Client ws(InetSocketAddress address) {
        String prefix = isSsl() ? "wss" : "ws";
        return ws(prefix.concat(address.toString()).concat(getHandshakeUrl()));
    }

    public final <C extends Client> C ws(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("invalid url '%s'", url), e);
        }
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        String host = uri.getHost() == null ? NetUtils.getLocalAddress().toString() : uri.getHost();
        int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
                throw new IllegalArgumentException(String.format("invlid port '%d'", port));
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(String.format("Only ws(s) is supported. now is '%s'", scheme));
        }

        boolean ssl = "wss".equalsIgnoreCase(scheme);
        if (ssl && !isSsl()) {
            throw new IllegalArgumentException("transport config not open ssl");
        }

        //websocker frame处理
        WsClientHandler wsClientHandler =
                new WsClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()), getTransportHandler());

        WsClientHandlerInitializer WSClientHandlerInitializer = handlerInitializer(wsClientHandler);
        C client = client(InetSocketAddress.createUnresolved(host, port));
        client.connect(this, WSClientHandlerInitializer);

        try {
            wsClientHandler.handshakeFuture().sync();
        } catch (InterruptedException e) {

        }

        return client;
    }

    protected WsClientHandlerInitializer handlerInitializer(WsClientHandler wsClientHandler) {
        return new WsClientHandlerInitializer(this, wsClientHandler);
    }

    protected <C extends Client> C client(InetSocketAddress address) {
        return (C) new Client(address);
    }
}
