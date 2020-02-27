package org.kin.transport.netty.core;

import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
@FunctionalInterface
public interface ProtocolHandler<T extends AbstractProtocol> {
    /**
     * 在channel线程调用, 最好内部捕获异常, 不然会导致channel因异常关闭
     * @param session 会话
     * @param protocol 协议
     */
    void handleProtocol(AbstractSession session, T protocol);
}
