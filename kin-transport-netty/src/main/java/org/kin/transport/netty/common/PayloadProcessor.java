package org.kin.transport.netty.common;

import reactor.core.publisher.Mono;

/**
 * payload逻辑处理
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
@FunctionalInterface
public interface PayloadProcessor {
    /**
     * payload逻辑处理
     * 该方法在netty channel event loop执行
     *
     * @param session channel session
     * @param payload ByteBuf payload
     * @return complete signal
     */
    Mono<Void> process(Session session, ByteBufPayload payload);
}
