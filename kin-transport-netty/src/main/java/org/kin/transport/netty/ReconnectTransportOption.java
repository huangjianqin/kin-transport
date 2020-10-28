package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * 支持重连的TransportOption
 *
 * @author huangjianqin
 * @date 2020/10/28
 */
public interface ReconnectTransportOption<MSG> {
    /**
     * 重连逻辑, 也就构造新Client的逻辑
     */
    Client<MSG> reconnect(InetSocketAddress address);

    /**
     * 包装支持自动重连的ProtocolHandler
     */
    void wrapProtocolHandler(ProtocolHandler<MSG> protocolHandler);
}
