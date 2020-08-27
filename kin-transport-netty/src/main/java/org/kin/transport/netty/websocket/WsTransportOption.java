package org.kin.transport.netty.websocket;

import org.kin.transport.netty.TransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public class WsTransportOption extends TransportOption {
    /** transport handler */
    private WsTransportHandler transportHandler;
    /**
     * 处理bytes流还是text流
     * bytes流=true
     * text流=false
     */
    private boolean binaryOrText;
    /** websocket 握手url */
    private String handshakeUrl = WsConstants.WS_PATH;

    //----------------------------------------------------------------------------------------------------------------
    public <T extends WsTransportOption> T transportHandler(WsTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends WsTransportOption> T text() {
        this.binaryOrText = false;
        return (T) this;
    }

    public <T extends WsTransportOption> T binary() {
        this.binaryOrText = true;
        return (T) this;
    }

    public <T extends WsTransportOption> T handshakeUrl(String handshakeUrl) {
        this.handshakeUrl = handshakeUrl;
        return (T) this;
    }

    //getter
    public WsTransportHandler getTransportHandler() {
        return transportHandler;
    }

    public boolean isBinaryOrText() {
        return binaryOrText;
    }

    public String getHandshakeUrl() {
        return handshakeUrl;
    }
}
