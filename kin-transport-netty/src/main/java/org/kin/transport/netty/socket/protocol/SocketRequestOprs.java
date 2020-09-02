package org.kin.transport.netty.socket.protocol;

/**
 * socket请求操作
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public interface SocketRequestOprs {
    /**
     * 获取协议号
     *
     * @return 协议号
     */
    int getProtocolId();

    /**
     * 获取不含协议id的协议内容字节数大小
     *
     * @return 不含协议id的协议内容字节数大小
     */
    int getContentSize();

    /**
     * 读byte
     *
     * @return 协议内容中的一个byte
     */
    byte readByte();

    /**
     * 读无符号byte
     *
     * @return 协议内容中的一个无符号byte
     */
    short readUnsignedByte();

    /**
     * 读boolean
     *
     * @return 协议内容中的一个boolean
     */
    boolean readBoolean();

    /**
     * 读bytes
     *
     * @param length bytes长度
     * @return 协议内容中指定长度的bytes
     */
    byte[] readBytes(int length);

    /**
     * 读Bytes
     *
     * @return 协议内容可读Bytes
     */
    byte[] readBytes();

    /**
     * 读short
     *
     * @return 协议内容中的一个short
     */
    short readShort();

    /**
     * 读无符号short
     *
     * @return 协议内容中的一个无符号short
     */
    int readUnsignedShort();

    /**
     * 读int
     *
     * @return 协议内容中的一个int
     */
    int readInt();

    /**
     * 读无符号int
     *
     * @return 协议内容中的一个无符号int
     */
    long readUnsignedInt();

    /**
     * 读float
     *
     * @return 协议内容中的一个float
     */
    float readFloat();

    /**
     * 读long
     *
     * @return 协议内容中的一个long
     */
    long readLong();

    /**
     * 读double
     *
     * @return 协议内容中的一个double
     */
    double readDouble();

    /**
     * 读String(short长度)
     *
     * @return 协议内容中的一个String(short长度)
     */
    String readString();

    /**
     * 读String(无符号short长度)
     *
     * @return 协议内容中的一个String(无符号short长度)
     */
    String readBigString();
}
