package org.kin.transport.netty.socket;

import com.google.common.util.concurrent.RateLimiter;
import org.kin.transport.netty.socket.protocol.ProtocolFactory;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议流控缓存
 *
 * @author huangjianqin
 * @date 2019-09-17
 */
public class ProtocolRateLimiter {
    private static final Map<Integer, RateLimiter> RATE_LIMITERS = new ConcurrentHashMap<>();

    private ProtocolRateLimiter() {
    }

    /**
     * 校验是否需要流控
     */
    @SuppressWarnings("unchecked")
    public static boolean valid(Object protocol) {
        if (protocol instanceof SocketProtocol) {
            int protocolId = ProtocolFactory.getProtocolId((Class<? extends SocketProtocol>) protocol.getClass());
            long rate = ProtocolFactory.getProtocolRate(protocolId);
            if (rate > 0) {
                RateLimiter rateLimiter = RATE_LIMITERS.computeIfAbsent(protocolId, k -> RateLimiter.create(rate));

                if (!rateLimiter.tryAcquire()) {
                    return false;
                }
            }
        }
        return true;
    }
}
