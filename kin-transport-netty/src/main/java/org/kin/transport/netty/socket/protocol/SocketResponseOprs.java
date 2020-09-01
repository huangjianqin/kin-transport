package org.kin.transport.netty.socket.protocol;

import io.netty.buffer.ByteBuf;

/**
 * @author huangjianqin
 * @date 2019/5/30
 */
public interface SocketResponseOprs {
    /**
     * 设置协议id
     *
     * @param protocolId 协议id
     * @return 该response
     */
    SocketResponseOprs setProtocolId(int protocolId);

    /**
     * 获取底层的bytebuf
     *
     * @return 底层的bytebuf
     */
    ByteBuf getByteBuf();

    /**
     * 获取协议id
     *
     * @return 协议id
     */
    int getProtocolId();

    /**
     * 获取协议大小
     *
     * @return 协议大小
     */
    int getSize();

    /**
     * 写byte
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeByte(int value);

    /**
     * 写无符号byte
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeUnsignedByte(short value);

    /**
     * 写boolean
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeBoolean(boolean value);

    /**
     * 写bytes
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeBytes(byte[] value);

    /**
     * 写short
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeShort(int value);

    /**
     * 写无符号short
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeUnsignedShort(int value);

    /**
     * 写int
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeInt(int value);

    /**
     * 写无符号int
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeUnsignedInt(long value);

    /**
     * 写float
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeFloat(float value);

    /**
     * 写long
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeLong(long value);

    /**
     * 写double
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeDouble(double value);

    /**
     * 写String(length为short)
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeString(String value);

    /**
     * 写String(length为无符号short)
     *
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs writeBigString(String value);

    /**
     * 在字节数组中某index开始设置boolean
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setBoolean(int index, boolean value);

    /**
     * 在字节数组中某index开始设置byte
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setByte(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号byte
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setUnsignedByte(int index, int value);

    /**
     * 在字节数组中某index开始设置short
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setShort(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号short
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setUnsignedShort(int index, int value);

    /**
     * 在字节数组中某index开始设置int
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setInt(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号int
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setUnsignedInt(int index, long value);

    /**
     * 在字节数组中某index开始设置long
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setLong(int index, long value);

    /**
     * 在字节数组中某index开始设置float
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setFloat(int index, float value);

    /**
     * 在字节数组中某index开始设置double
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setDouble(int index, double value);

    /**
     * 在字节数组中某index开始设置bytes
     *
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    SocketResponseOprs setBytes(int index, byte[] value);
}
