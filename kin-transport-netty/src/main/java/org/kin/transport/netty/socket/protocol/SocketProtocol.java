package org.kin.transport.netty.socket.protocol;

/**
 * 协议抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class SocketProtocol {
    /** 协议id */
    private int protocolId;
    /** 创建时间 */
    private long createTime = System.currentTimeMillis();

    public SocketProtocol() {
    }

    public SocketProtocol(int protocolId) {
        this.protocolId = protocolId;
    }

    protected void beforeRead(SocketRequestOprs request) {

    }

    /**
     * in解析
     *
     * @param request in协议(本质上是个byteBuf的封装)
     */
    public abstract void read(SocketRequestOprs request);

    /**
     * out封装
     *
     * @param response out协议(本质上是个byteBuf的封装
     */
    public abstract void write(SocketResponseOprs response);

    public SocketResponseOprs write() {
        SocketResponseOprs response = new SocketProtocolByteBuf(protocolId);
        write(response);
        return response;
    }

    @Override
    public String toString() {
        return "Protocol<" + getClass().getSimpleName() + ">{" +
                "protocolId=" + protocolId +
                ", createTime=" + createTime +
                '}';
    }

    //setter && getter

    public int getProtocolId() {
        return protocolId;
    }

    public long getCreateTime() {
        return createTime;
    }
}
