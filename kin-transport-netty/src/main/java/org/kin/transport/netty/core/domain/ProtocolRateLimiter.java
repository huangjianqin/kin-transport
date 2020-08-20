package org.kin.transport.netty.core.domain;

import com.google.common.util.concurrent.RateLimiter;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.protocol.ProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议流控缓存
 *
 * @author huangjianqin
 * @date 2019-09-17
 */
public class ProtocolRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(ProtocolRateLimiter.class);
    private static final Map<Integer, RateLimiter> RATE_LIMITERS = new ConcurrentHashMap<>();

    private ProtocolRateLimiter() {
    }

    /**
     * 校验是否需要流控
     */
    public static <T extends AbstractProtocol> boolean valid(T protocol) {
        int protocolId = protocol.getProtocolId();
        long rate = ProtocolFactory.getProtocolRate(protocolId);
        if (rate > 0) {
            RateLimiter rateLimiter = RATE_LIMITERS.computeIfAbsent(protocolId, k -> RateLimiter.create(rate));

            if (!rateLimiter.tryAcquire()) {
                return false;
            }
        }

        return true;
    }
}
