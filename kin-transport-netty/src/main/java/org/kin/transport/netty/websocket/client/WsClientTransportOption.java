package org.kin.transport.netty.websocket.client;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;
import org.kin.transport.netty.websocket.client.handler.WsClientHandler;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsClientTransportOption<MSG, INOUT extends WebSocketFrame>
        extends AbstractWsTransportOption<MSG, INOUT, WsClientTransportOption<MSG, INOUT>> {
    public final Client<MSG> build(InetSocketAddress address) {
        String prefix = isSsl() ? "wss" : "ws";
        return build(prefix.concat(address.toString()).concat(getHandshakeUrl()));
    }

    public final Client<MSG> build(String url) {
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
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

        WsClientHandlerInitializer<MSG, INOUT> handlerInitializer = new WsClientHandlerInitializer<>(this, wsClientHandler);
        Client<MSG> client = new Client<>(new InetSocketAddress(host, port));
        client.connect(this, handlerInitializer);

        try {
            wsClientHandler.handshakeFuture().sync();
        } catch (InterruptedException e) {

        }

        return client;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<INOUT, MSG, INOUT> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            return getDefaultTransportProtocolTransfer(false);
        }

        return super.getTransportProtocolTransfer();
    }
}
