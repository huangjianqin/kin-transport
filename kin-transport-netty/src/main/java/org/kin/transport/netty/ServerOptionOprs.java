package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * server options操作
 *
 * @author huangjianqin
 * @date 2020/12/11
 */
public interface ServerOptionOprs<S> {
    /**
     * 绑定端口
     */
    S bind(InetSocketAddress address);
}
