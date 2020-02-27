package org.kin.transport.netty.core;

import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.concurrent.PartitionTaskExecutor;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2019-09-17
 */
public class ProtocolRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(ProtocolRateLimiter.class);
    /** 一个协议一条线程处理 */
    private static final PartitionTaskExecutor<Integer> EXECUTORS = new PartitionTaskExecutor<>(5);
    private static final Map<Integer, Long> RATE_DATA = new HashMap<>();

    static {
        JvmCloseCleaner.DEFAULT().add(EXECUTORS::shutdown);
    }

    private ProtocolRateLimiter() {
    }

    /**
     * 校验是否需要限流
     */
    public static <T extends AbstractProtocol> boolean valid(T protocol, ProtocolHandler<T> protocolHandler){
        int protocolId = protocol.getProtocolId();
        long rate = ProtocolFactory.getProtocolRate(protocolId);
        if(rate > 0){
            long now = System.currentTimeMillis();
            long lastTime = RATE_DATA.getOrDefault(protocolId, 0L);
            if(lastTime == 0){
                lastTime = now;
            }

            RATE_DATA.put(protocolId, now);
            if(now - lastTime <= rate){
                ProtocolRateLimitCallback callback = ProtocolFactory.getProtocolRateLimitCallback(protocolId);
                if(callback != null){
                    long finalLastTime = lastTime;
                    EXECUTORS.execute(protocolId, () -> {
                        log.warn("protocol(id={},lastTime={},now={}) rate limit >>> {}", protocolId, finalLastTime, now, protocol);
                        callback.call(protocol, protocolHandler);
                    });
                }
                return false;
            }
        }

        return true;
    }
}
