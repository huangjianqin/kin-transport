package org.kin.transport.netty.core;

import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 * @author huangjianqin
 * @date 2019-09-17
 */
public class DoNothingRateLimitCallback implements ProtocolRateLimitCallback {
    @Override
    public <T extends AbstractProtocol> void call(T protocol, ProtocolHandler<T> protocolHandler) {
        //TODO do nothing
    }
}
