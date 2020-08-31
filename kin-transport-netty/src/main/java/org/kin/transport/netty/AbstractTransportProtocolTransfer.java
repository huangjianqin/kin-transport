package org.kin.transport.netty;

/**
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
