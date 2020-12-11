package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * client options操作
 *
 * @author huangjianqin
 * @date 2020/12/11
 */
public interface ClientOptionOprs<C> {
    /**
     * 连接远程服务器
     */
    C connect(InetSocketAddress address);

    /**
     * 连接远程服务器, 支持自动重连, 默认支持缓存发送失败的协议, 直到发送成功(缓存协议数量为200)
     */
    default C withReconnect(InetSocketAddress address) {
        return withReconnect(address, true);
    }

    /**
     * 连接远程服务器, 支持自动重连
     */
    default C withReconnect(InetSocketAddress address, boolean cacheMessage) {
        throw new UnsupportedOperationException();
    }
}
