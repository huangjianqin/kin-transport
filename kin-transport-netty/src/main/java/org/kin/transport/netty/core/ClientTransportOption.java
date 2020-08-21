package org.kin.transport.netty.core;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.socket.SocketHandlerInitializer;
import org.kin.transport.netty.websocket.WSConstants;
import org.kin.transport.netty.websocket.WsClientHandlerInitializer;
import org.kin.transport.netty.websocket.handler.WSClientHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * client transport配置
 *
 * @author huangjianqin
 * @date 2019-09-13
 */
public class ClientTransportOption extends TransportOption {
    public Client tcp(InetSocketAddress address) {
        ChannelHandlerInitializer channelHandlerInitializer = new SocketHandlerInitializer(this, false);
        Client client = new Client(address);
        client.connect(this, channelHandlerInitializer);
        return client;
    }

    public Client ws(InetSocketAddress address) {
        String prefix = isSsl() ? "wss" : "ws";
        return ws(prefix.concat(address.toString()).concat(WSConstants.WS_PATH));
    }

    public Client ws(String url) {
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
        WSClientHandler wsClientHandler =
                new WSClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        WsClientHandlerInitializer WSClientHandlerInitializer = new WsClientHandlerInitializer(this, wsClientHandler);
        Client client = new Client(InetSocketAddress.createUnresolved(host, port));
        client.connect(this, WSClientHandlerInitializer);

        try {
            wsClientHandler.handshakeFuture().sync();
        } catch (InterruptedException e) {

        }

        return client;
    }
}
