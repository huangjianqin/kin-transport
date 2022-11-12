package org.kin.transport.netty;

import reactor.netty.transport.ServerTransport;

/**
 * 自定义{@link ServerTransport}配置
 * @author huangjianqin
 * @date 2022/11/9
 */
@FunctionalInterface
public interface ServerTransportCustomizer {
    /**
     * 自定义{@link ServerTransport}配置
     * @param transport server transport
     * @return 返回配置后的server transport
     */
    <ST extends ServerTransport<?, ?>> ST custom(ST transport);
}
