package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;

/**
 * 变长整形工具类
 *
 * @author huangjianqin
 * @date 2021/7/31
 */
public final class VarIntUtils {
    private VarIntUtils() {
    }

    /**
     * read 变长 32位int
     */
    public static int readRawVarInt32(ByteBuf byteBuf) {
        return readRawVarInt32(byteBuf, true);
    }

    /**
     * read 变长 32位int
     */
    public static int readRawVarInt32(ByteBuf byteBuf, boolean zigzag) {
        return org.kin.framework.utils.VarIntUtils.readRawVarInt32(new ByteBufInput(byteBuf), zigzag);
    }

    /**
     * read 变长 32位int
     */
    public static void writeRawVarInt32(ByteBuf byteBuf, int value) {
        writeRawVarInt32(byteBuf, value, true);
    }

    /**
     * read 变长 32位int
     */
    public static void writeRawVarInt32(ByteBuf byteBuf, int value, boolean zigzag) {
        org.kin.framework.utils.VarIntUtils.writeRawVarInt32(new ByteBufOutput(byteBuf), value, zigzag);
    }

    /**
     * read 变长 64位int
     */
    public static long readRawVarInt64(ByteBuf byteBuf) {
        return readRawVarInt64(byteBuf, true);
    }

    /**
     * read 变长 64位int
     */
    public static long readRawVarInt64(ByteBuf byteBuf, boolean zigzag) {
        return org.kin.framework.utils.VarIntUtils.readRawVarInt64(new ByteBufInput(byteBuf), zigzag);
    }

    /**
     * read 变长 64位int
     */
    public static void writeRawVarInt64(ByteBuf byteBuf, long value) {
        writeRawVarInt64(byteBuf, value, true);
    }

    /**
     * read 变长 64位int
     */
    public static void writeRawVarInt64(ByteBuf byteBuf, long value, boolean zigzag) {
        org.kin.framework.utils.VarIntUtils.writeRawVarInt64(new ByteBufOutput(byteBuf), value, zigzag);
    }
}
