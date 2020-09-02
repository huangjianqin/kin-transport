package org.kin.transport.netty;

/**
 * 传输层 <-> 协议层 数据转换逻辑实现抽象
 *
 * @author huangjianqin
 * @date 2020/8/28
 */
public abstract class AbstractTransportProtocolTransfer<IN, MSG, OUT> implements TransportProtocolTransfer<IN, MSG, OUT> {
    /** 是否压缩 */
    protected final boolean compression;

    public AbstractTransportProtocolTransfer(boolean compression) {
        this.compression = compression;
    }
}
