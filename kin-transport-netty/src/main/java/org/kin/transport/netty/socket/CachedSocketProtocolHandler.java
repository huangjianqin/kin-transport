package org.kin.transport.netty.socket;

import org.kin.transport.netty.CachedProtocolHandler;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.concurrent.TimeUnit;

/**
 * socket协议层逻辑实现
 *
 * @author huangjianqin
 * @date 2021/1/20
 */
public abstract class CachedSocketProtocolHandler extends CachedProtocolHandler<SocketProtocol> {
    public CachedSocketProtocolHandler(int cacheTtl, TimeUnit ttlTimeUnit) {
        super(cacheTtl, ttlTimeUnit);
    }
}
