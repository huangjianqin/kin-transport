package org.kin.transport.netty;

import com.google.common.base.Preconditions;

/**
 * 服务端传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class ServerTransport extends Transport {
    /** 绑定端口 */
    private int port;

    @Override
    protected void checkRequire() {
        super.checkRequire();
        Preconditions.checkArgument(port > 0, "server port must be greater than 0");
    }

    //setter && getter
    public int getPort() {
        return port;
    }

    public ServerTransport port(int port) {
        this.port = port;
        return this;
    }
}
