package org.kin.transport.netty.websocket.server;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.transport.netty.Server;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * websocket server 传输配置
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsServerTransportOption<MSG, INOUT extends WebSocketFrame>
        extends AbstractWsTransportOption<MSG, INOUT, WsServerTransportOption<MSG, INOUT>> {
    /**
     * 构建websocket server
     */
    public final Server bind(InetSocketAddress address) {
        WsServerHandlerInitializer<MSG, INOUT> handlerInitializer = new WsServerHandlerInitializer<>(this);
        Server server = new Server(this, handlerInitializer);
        server.bind(address);
        return server;
    }

    //----------------------------------------------------------------------------------------------------------------
    @Override
    public TransportProtocolTransfer<INOUT, MSG, INOUT> getTransportProtocolTransfer() {
        if (Objects.isNull(super.getTransportProtocolTransfer())) {
            //默认
            return getDefaultTransportProtocolTransfer(true);
        }

        return super.getTransportProtocolTransfer();
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static <MSG, INOUT extends WebSocketFrame> WsServerTransportOptionBuilder<MSG, INOUT> builder() {
        return new WsServerTransportOptionBuilder<>();
    }

    public static class WsServerTransportOptionBuilder<MSG, INOUT extends WebSocketFrame>
            extends WsTransportOptionBuilder<MSG, INOUT, WsServerTransportOption<MSG, INOUT>, WsServerTransportOptionBuilder<MSG, INOUT>> {
        public WsServerTransportOptionBuilder() {
            super(new WsServerTransportOption<>());
        }
    }
}
