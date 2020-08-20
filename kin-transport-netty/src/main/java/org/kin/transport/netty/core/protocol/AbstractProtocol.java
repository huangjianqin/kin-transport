package org.kin.transport.netty.core.protocol;

import org.kin.transport.netty.core.protocol.domain.ProtocolByteBuf;
import org.kin.transport.netty.core.protocol.domain.Request;
import org.kin.transport.netty.core.protocol.domain.Response;

/**
 * 协议抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class AbstractProtocol {
    /** 协议id */
    private int protocolId;
    /** 创建时间 */
    private long createTime = System.currentTimeMillis();

    public AbstractProtocol() {
    }

    public AbstractProtocol(int protocolId) {
        this.protocolId = protocolId;
    }

    protected void beforeRead(Request request) {

    }

    /**
     * in解析
     * @param request in协议(本质上是个byteBuf的封装)
     */
    public abstract void read(Request request);

    /**
     * out封装
     * @param response out协议(本质上是个byteBuf的封装
     */
    public abstract void write(Response response);

    public Response write() {
        Response response = new ProtocolByteBuf(protocolId);
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
