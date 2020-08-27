package org.kin.transport.netty.websocket;

import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.websocket.client.WsClientTransportOption;
import org.kin.transport.netty.websocket.server.WsServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractWsTransportOption extends AbstractTransportOption {
    public static final AbstractWsTransportOption INSTANCE = new AbstractWsTransportOption() {
    };

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

    /** server配置 */
    public static WsServerTransportOption server() {
        return new WsServerTransportOption();
    }

    /** client配置 */
    public static WsClientTransportOption client() {
        return new WsClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
    public <T extends AbstractWsTransportOption> T transportHandler(WsTransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends AbstractWsTransportOption> T text() {
        this.binaryOrText = false;
        return (T) this;
    }

    public <T extends AbstractWsTransportOption> T binary() {
        this.binaryOrText = true;
        return (T) this;
    }

    public <T extends AbstractWsTransportOption> T handshakeUrl(String handshakeUrl) {
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
