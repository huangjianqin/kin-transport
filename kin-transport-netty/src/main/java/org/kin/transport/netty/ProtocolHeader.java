package org.kin.transport.netty;

/**
 * 协议头, 由协议内容长度(4个字节组成)+魔数bytes, 数据内容由使用者负责解析
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class ProtocolHeader {
    /** 读取的magic bytes */
    private final byte[] magicBytes;
    /**
     * 协议内容长度
     *
     * @see Protocols#PROTOCOL_LENGTH_MARK_BYTES
     */
    private int length;

    public ProtocolHeader(int magicSize) {
        this.magicBytes = new byte[magicSize];
    }

    /**
     * 获取body数据内容长度
     */
    public int getBodySize() {
        return length - magicBytes.length;
    }

    //setter && getter
    public byte[] getMagicBytes() {
        return magicBytes;
    }

    public int getLength() {
        return length;
    }

    public ProtocolHeader length(int length) {
        this.length = length;
        return this;
    }
}
