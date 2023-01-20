package org.kin.transport.netty.common;

import io.netty.channel.ChannelHandler;

import java.util.Collections;
import java.util.List;

/**
 * 定义前置handler, 比如idle, 流量整形和flush优化handler
 * 注意, 这些handler不能影响协议解析
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
@FunctionalInterface
public interface PreHandlerInitializer {
    PreHandlerInitializer DEFAULT = new PreHandlerInitializer() {
        @Override
        public <PT extends ProtocolTransport<PT>> List<ChannelHandler> preHandlers(PT transport) {
            return Collections.emptyList();
        }
    };

    /** 定义前置handler */
    <ATT extends ProtocolTransport<ATT>> List<ChannelHandler> preHandlers(ATT transport);
}
