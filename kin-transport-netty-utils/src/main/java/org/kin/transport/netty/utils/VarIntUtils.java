package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import org.kin.framework.utils.VarIntInput;
import org.kin.framework.utils.VarIntOutput;

/**
 * 变长整形工具类
 *
 * @author huangjianqin
 * @date 2021/7/31
 */
public final class VarIntUtils {
    private VarIntUtils() {
    }

    public static int readRawVarInt32(ByteBuf byteBuf) {
        return readRawVarInt32(byteBuf, true);
    }

    public static int readRawVarInt32(ByteBuf byteBuf, boolean zigzag) {
        return org.kin.framework.utils.VarIntUtils.readRawVarInt32(new ByteBufInput(byteBuf), zigzag);
    }

    public static void writeRawVarInt32(ByteBuf byteBuf, int value) {
        writeRawVarInt32(byteBuf, value, true);
    }

    public static void writeRawVarInt32(ByteBuf byteBuf, int value, boolean zigzag) {
        org.kin.framework.utils.VarIntUtils.writeRawVarInt32(new ByteBufOutput(byteBuf), value, zigzag);
    }

    public static long readRawVarLong64(ByteBuf byteBuf) {
        return readRawVarLong64(byteBuf, true);
    }

    public static long readRawVarLong64(ByteBuf byteBuf, boolean zigzag) {
        return org.kin.framework.utils.VarIntUtils.readRawVarLong64(new ByteBufInput(byteBuf), zigzag);
    }

    public static void writeRawVarLong64(ByteBuf byteBuf, long value) {
        writeRawVarLong64(byteBuf, value, true);
    }

    public static void writeRawVarLong64(ByteBuf byteBuf, long value, boolean zigzag) {
        org.kin.framework.utils.VarIntUtils.writeRawVarLong64(new ByteBufOutput(byteBuf), value, zigzag);
    }

    /**
     * 基于{@link ByteBuf}的{@link VarIntInput}实现
     */
    private static class ByteBufInput implements VarIntInput {
        private final ByteBuf byteBuf;

        public ByteBufInput(ByteBuf byteBuf) {
            this.byteBuf = byteBuf;
        }

        @Override
        public byte readByte() {
            return byteBuf.readByte();
        }

        @Override
        public int readerIndex() {
            return byteBuf.readerIndex();
        }

        @Override
        public void readerIndex(int readerIndex) {
            byteBuf.readerIndex(readerIndex);
        }

        @Override
        public boolean readerIndexSupported() {
            return true;
        }

        @Override
        public int readableBytes() {
            return byteBuf.readableBytes();
        }
    }

    /**
     * 基于{@link ByteBuf}的{@link VarIntOutput}实现
     */
    private static class ByteBufOutput implements VarIntOutput {
        private final ByteBuf byteBuf;

        public ByteBufOutput(ByteBuf byteBuf) {
            this.byteBuf = byteBuf;
        }

        @Override
        public void writeByte(int value) {
            byteBuf.writeByte(value);
        }

        @Override
        public int writableBytes() {
            //认为是可无限写入, 因为ByteBuf会自动扩容
            return Integer.MAX_VALUE;
        }
    }
}
