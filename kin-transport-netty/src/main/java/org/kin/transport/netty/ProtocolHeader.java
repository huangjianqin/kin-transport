package org.kin.transport.netty;

/**
 * 协议头
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class ProtocolHeader {
    /** 读取的magic bytes */
    private final byte[] magicBytes;
    /** 数据内容长度 */
    private int bodySize;

    public ProtocolHeader(int magicSize) {
        this.magicBytes = new byte[magicSize];
    }

    //setter && getter
    public byte[] getMagicBytes() {
        return magicBytes;
    }

    public int getBodySize() {
        return bodySize;
    }

    public ProtocolHeader bodySize(int bodySize) {
        this.bodySize = bodySize;
        return this;
    }
}
