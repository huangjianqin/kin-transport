package org.kin.transport.netty.socket.protocol;

/**
 * 协议抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class SocketProtocol {
    /** 协议id */
    @Ignore
    private int protocolId;
    /** 创建时间 */
    private final long createTime = System.currentTimeMillis();

    @Override
    public String toString() {
        return "Protocol<" + getClass().getSimpleName() + ">{" +
                "protocolId=" + protocolId +
                ", createTime=" + createTime +
                '}';
    }

    //getter
    public int getProtocolId() {
        return protocolId;
    }

    public long getCreateTime() {
        return createTime;
    }
}
