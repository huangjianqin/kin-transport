package org.kin.transport.netty.socket.protocol;

/**
 * 协议抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class SocketProtocol {
    /** 创建时间 */
    @Ignore
    private final transient long createTime = System.currentTimeMillis();

    //getter
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return "SocketProtocol{" +
                "createTime=" + createTime +
                '}';
    }
}
