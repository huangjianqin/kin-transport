package org.kin.transport.netty.websocket;

import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.TransportProtocolTransfer;

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

    //----------------------------------------------------------------------------------------------------------------

    /**
     * 获取默认的传输层与协议层数据转换实现
     */
    protected TransportProtocolTransfer<INOUT, MSG, INOUT> getDefaultTransportProtocolTransfer(boolean serverOrClient) {
        /*
         * MSG 实现了 {@link AbstractSocketProtocol}
         * INOUT 是 {@link BinaryWebSocketFrame}
         */
        return (TransportProtocolTransfer<INOUT, MSG, INOUT>)
                new WsBinaryTransfer(serverOrClient);
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
