package org.kin.transport.netty.core.protocol.domain;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public interface Response {
    /**
     * 设置协议id
     *
     * @param protocolId 协议id
     * @return 该response
     */
    Response setProtocolId(int protocolId);

    /**
     * 获取底层的bytebuf
     * @return 底层的bytebuf
     */
    ByteBuf getByteBuf();

    /**
     * 获取协议id
     * @return 协议id
     */
    int getProtocolId();

    /**
     * 获取协议大小
     * @return 协议大小
     */
    int getSize();

    /**
     * 写byte
     * @param value 值
     * @return 该response
     */
    Response writeByte(int value);

    /**
     * 写无符号byte
     * @param value 值
     * @return 该response
     */
    Response writeUnsignedByte(short value);

    /**
     * 写boolean
     * @param value 值
     * @return 该response
     */
    Response writeBoolean(boolean value);

    /**
     * 写bytes
     * @param value 值
     * @return 该response
     */
    Response writeBytes(byte[] value);

    /**
     * 写short
     * @param value 值
     * @return 该response
     */
    Response writeShort(int value);

    /**
     * 写无符号short
     * @param value 值
     * @return 该response
     */
    Response writeUnsignedShort(int value);

    /**
     * 写int
     * @param value 值
     * @return 该response
     */
    Response writeInt(int value);

    /**
     * 写无符号int
     * @param value 值
     * @return 该response
     */
    Response writeUnsignedInt(long value);

    /**
     * 写float
     * @param value 值
     * @return 该response
     */
    Response writeFloat(float value);

    /**
     * 写long
     * @param value 值
     * @return 该response
     */
    Response writeLong(long value);

    /**
     * 写double
     * @param value 值
     * @return 该response
     */
    Response writeDouble(double value);

    /**
     * 写String(length为short)
     * @param value 值
     * @return 该response
     */
    Response writeString(String value);

    /**
     * 写String(length为无符号short)
     * @param value 值
     * @return 该response
     */
    Response writeBigString(String value);

    /**
     * 在字节数组中某index开始设置boolean
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setBoolean(int index, boolean value);

    /**
     * 在字节数组中某index开始设置byte
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setByte(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号byte
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setUnsignedByte(int index, int value);

    /**
     * 在字节数组中某index开始设置short
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setShort(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号short
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setUnsignedShort(int index, int value);

    /**
     * 在字节数组中某index开始设置int
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setInt(int index, int value);

    /**
     * 在字节数组中某index开始设置无符号int
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setUnsignedInt(int index, long value);

    /**
     * 在字节数组中某index开始设置long
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setLong(int index, long value);

    /**
     * 在字节数组中某index开始设置float
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setFloat(int index, float value);

    /**
     * 在字节数组中某index开始设置double
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setDouble(int index, double value);

    /**
     * 在字节数组中某index开始设置bytes
     * @param index 下标
     * @param value 值
     * @return 该response
     */
    Response setBytes(int index, byte[] value);
}
