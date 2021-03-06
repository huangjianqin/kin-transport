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
    /** websocket 握手url */
    protected String handshakeUrl = WsConstants.WS_PATH;

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

    //getter
    public String getHandshakeUrl() {
        return handshakeUrl;
    }


    //------------------------------------------------------builder------------------------------------------------------
    public static class WsTransportOptionBuilder<MSG, INOUT extends WebSocketFrame, O extends AbstractWsTransportOption<MSG, INOUT, O>, B extends WsTransportOptionBuilder<MSG, INOUT, O, B>>
            extends TransportOptionBuilder<INOUT, MSG, INOUT, O, B> {
        public WsTransportOptionBuilder(O transportOption) {
            super(transportOption);
        }

        @SuppressWarnings("unchecked")
        public B handshakeUrl(String handshakeUrl) {
            transportOption.handshakeUrl = handshakeUrl;
            return (B) this;
        }
    }
}
