package org.kin.transport.netty;

import org.kin.transport.netty.http.AbstractHttpTransportOption;
import org.kin.transport.netty.socket.AbstractSocketTransportOption;
import org.kin.transport.netty.websocket.AbstractWsTransportOption;

/**
 * net transport
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public class Transports {
    /**
     * socket transport 配置
     */
    public static AbstractSocketTransportOption socket() {
        return AbstractSocketTransportOption.INSTANCE;
    }

    /**
     * websocket transport 配置
     */
    public static AbstractWsTransportOption websocket() {
        return AbstractWsTransportOption.INSTANCE;
    }

    /**
     * http transport 配置
     */
    public static AbstractHttpTransportOption http() {
        return AbstractHttpTransportOption.INSTANCE;
    }
}
