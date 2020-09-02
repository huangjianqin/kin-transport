package org.kin.transport.netty;

import java.net.InetSocketAddress;

/**
 * connection 抽象
 *
 * @author 健勤
 * @date 2017/2/10
 */
public abstract class AbstractConnection {
    /** 连接地址 */
    protected final InetSocketAddress address;

    public AbstractConnection(InetSocketAddress address) {
        this.address = address;
    }

    /**
     * 连接关闭
     */
    public abstract void close();

    /**
     * @return 绑定地址 or 远程服务器地址
     */
    public String getAddress() {
        return address.getHostName() + ":" + address.getPort();
    }

    /**
     * 检查连接是否有效
     *
     * @return 连接是否有效
     */
    public abstract boolean isActive();
}
