package org.kin.transport.netty.websocket;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.framework.utils.ClassUtils;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;
import org.kin.transport.netty.websocket.client.WsClientTransportOption;
import org.kin.transport.netty.websocket.server.WsServerTransportOption;

import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractWsTransportOption<MSG, INOUT extends WebSocketFrame, O extends AbstractWsTransportOption<MSG, INOUT, O>>
        extends AbstractTransportOption<INOUT, MSG, INOUT, O> {
    public static final AbstractWsTransportOption INSTANCE = new AbstractWsTransportOption() {
    };

    /** websocket 握手url */
    private String handshakeUrl = WsConstants.WS_PATH;

    /** binary server配置 */
    public <MSG> WsServerTransportOption<MSG, BinaryWebSocketFrame> binaryServer() {
        return new WsServerTransportOption<>();
    }

    /** binary client配置 */
    public <MSG> WsClientTransportOption<MSG, BinaryWebSocketFrame> binaryClient() {
        return new WsClientTransportOption<>();
    }

    /** text server配置 */
    public <MSG> WsServerTransportOption<MSG, TextWebSocketFrame> textServer() {
        return new WsServerTransportOption<>();
    }

    /** text client配置 */
    public <MSG> WsClientTransportOption<MSG, TextWebSocketFrame> textClient() {
        return new WsClientTransportOption<>();
    }

    //----------------------------------------------------------------------------------------------------------------
    protected TransportProtocolTransfer<INOUT, MSG, INOUT> getDefaultTransportProtocolTransfer(boolean serverOrClient) {
        //默认 TransportProtocolTransfer
        List<Class<?>> genericTypes = ClassUtils.getSuperClassGenericActualTypes(getClass());
        if (AbstractSocketProtocol.class.isAssignableFrom(genericTypes.get(1)) && BinaryWebSocketFrame.class.equals(genericTypes.get(0))) {
            /**
             * MSG 实现了 {@link AbstractSocketProtocol}
             * INOUT 是 {@link BinaryWebSocketFrame}
             */
            return (TransportProtocolTransfer<INOUT, MSG, INOUT>)
                    new WsTransportProtocolTransfer(isCompression(), serverOrClient, getGlobalRateLimit());
        }

        return null;
    }

    //----------------------------------------------------------------------------------------------------------------
    public O handshakeUrl(String handshakeUrl) {
        this.handshakeUrl = handshakeUrl;
        return (O) this;
    }

    //getter
    public String getHandshakeUrl() {
        return handshakeUrl;
    }
}
