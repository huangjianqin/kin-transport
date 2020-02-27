package org.kin.transport.netty.core;

import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 * @author huangjianqin
 * @date 2019-09-17
 */
public interface ProtocolRateLimitCallback {
    /**
     * 协议限流回调
     *
     * @param protocol        协议对象
     * @param protocolHandler 协议处理
     * @param <T>             协议实现类
     */
    <T extends AbstractProtocol> void call(T protocol, ProtocolHandler<T> protocolHandler);
}
