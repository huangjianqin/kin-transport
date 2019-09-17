package org.kin.transport.netty.core;

import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 * @author huangjianqin
 * @date 2019-09-17
 */
public interface ProtocolRateLimitCallback {
    <T extends AbstractProtocol> void call(T protocol, ProtocolHandler<T> protocolHandler);
}
