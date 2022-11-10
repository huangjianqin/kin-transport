package org.kin.transport.netty;

import reactor.netty.transport.Transport;

/**
 * 自定义{@link Transport}配置
 *
 * @author huangjianqin
 * @date 2022/11/9
 */
@FunctionalInterface
public interface TransportCustomizer {
    /**
     * 自定义{@link Transport}配置
     *
     * @param transport transport
     * @return 返回配置后的transport
     */
    <T extends Transport<?, ?>> T custom(T transport);
}
