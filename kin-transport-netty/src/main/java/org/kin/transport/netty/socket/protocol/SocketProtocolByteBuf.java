package org.kin.transport.netty.socket.protocol;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCounted;

import java.nio.charset.StandardCharsets;

/**
 * 协议buffer内容封装
 *
 * @author huangjianqin
 * @date 2019/6/4
 */
public class SocketProtocolByteBuf implements SocketRequestOprs, SocketResponseOprs, ReferenceCounted {
    /** 读模式 */
    private static final int READ_MODE = 0;
    /** 写模式 */
    private static final int WRITE_MODE = 1;
    /** 读写模式 */
    private static final int READ_WRITE_MODE = 2;

    /** 字节buffer */
    private ByteBuf byteBuf;
    /** 协议id */
    private int protocolId;
    /** 协议成都 */
    private int contentSize;
    /** 模式 */
    private final int mode;

    public SocketProtocolByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.protocolId = readVarInt32();
        this.contentSize = byteBuf.readableBytes();
        this.mode = READ_MODE;
    }

    public SocketProtocolByteBuf(int protocolId) {
        byteBuf = Unpooled.buffer();
        this.protocolId = protocolId;
        this.mode = WRITE_MODE;
        writeVarInt32(protocolId);
    }

    //--------------------------------------------request----------------------------------------------------

    @Override
    public int getContentSize() {
        Preconditions.checkArgument(mode == READ_MODE);
        return contentSize;
    }

    @Override
    public byte readByte() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedByte();
    }

    @Override
    public boolean readBoolean() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readBoolean();
    }

    @Override
    public byte[] readBytes(int length) {
        Preconditions.checkArgument(mode == READ_MODE);
        Preconditions.checkArgument(length > 0);
        Preconditions.checkArgument(length <= byteBuf.readableBytes());
        byte[] result = new byte[length];
        byteBuf.readBytes(result);
        return result;
    }

    @Override
    public byte[] readBytes() {
        Preconditions.checkArgument(mode == READ_MODE);
        byte[] result = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }

    @Override
    public short readShort() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedShort();
    }

    @Override
    public int readInt() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readUnsignedInt();
    }

    @Override
    public int readVarInt32() {
        Preconditions.checkArgument(mode == READ_MODE);
        return readRawVarInt32();
    }

    @Override
    public float readFloat() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readFloat();
    }

    @Override
    public long readLong() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readLong();
    }

    @Override
    public long readVarLong64() {
        Preconditions.checkArgument(mode == READ_MODE);
        return readRawVarLong64();
    }

    @Override
    public double readDouble() {
        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.readDouble();
    }

    @Override
    public String readString() {
        Preconditions.checkArgument(mode == READ_MODE);
        int length = byteBuf.readShort();
        byte[] content = new byte[length];
        byteBuf.readBytes(content);
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public String readBigString() {
        Preconditions.checkArgument(mode == READ_MODE);
        int length = byteBuf.readUnsignedShort();
        byte[] content = new byte[length];
        byteBuf.readBytes(content);
        return new String(content, StandardCharsets.UTF_8);
    }


    @Override
    public int refCnt() {
//        Preconditions.checkArgument(mode == READ_MODE);
        return byteBuf.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
//        Preconditions.checkArgument(mode == READ_MODE);
        byteBuf.retain();
        return this;
    }

    @Override
    public ReferenceCounted retain(int i) {
        byteBuf.retain(i);
        return this;
    }

    @Override
    public ReferenceCounted touch() {
        byteBuf.touch();
        return this;
    }

    @Override
    public ReferenceCounted touch(Object o) {
        byteBuf.touch(o);
        return this;
    }

    @Override
    public boolean release() {
        return byteBuf.release();
    }

    @Override
    public boolean release(int i) {
        return byteBuf.release(i);
    }

    //--------------------------------------------response----------------------------------------------------

    @Override
    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    @Override
    public int getProtocolId() {
        return protocolId;
    }

    @Override
    public int getSize() {
        Preconditions.checkArgument(mode == WRITE_MODE);
        return byteBuf.readableBytes();
    }

    @Override
    public SocketResponseOprs writeByte(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE, "value: %s", value);
        byteBuf.writeByte(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedByte(short value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Byte.MAX_VALUE - Byte.MIN_VALUE, "value: %s", value);
        byteBuf.writeByte(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeBoolean(boolean value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeByte(value ? 1 : 0);
        return this;
    }

    @Override
    public SocketResponseOprs writeBytes(byte[] value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byteBuf.writeBytes(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeShort(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE, "value: %s", value);
        byteBuf.writeShort(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedShort(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Short.MAX_VALUE - Short.MIN_VALUE, "value: %s", value);
        byteBuf.writeShort(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeInt(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeInt(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeUnsignedInt(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeInt((int) value);
        return this;
    }

    @Override
    public SocketResponseOprs writeVarInt32(int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeRawVarInt32(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeFloat(float value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Float.MIN_VALUE && value <= Float.MAX_VALUE, "value: %s", value);
        byteBuf.writeFloat(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeLong(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.writeLong(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeVarLong64(long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeRawVarlong64(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeDouble(double value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Double.MIN_VALUE && value <= Double.MAX_VALUE, "value: %s", value);
        byteBuf.writeDouble(value);
        return this;
    }

    private void writeString0(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byte[] content = value.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(content.length);
        byteBuf.writeBytes(content);
    }

    @Override
    public SocketResponseOprs writeString(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeString0(value);
        return this;
    }

    @Override
    public SocketResponseOprs writeBigString(String value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        writeString0(value);
        return this;
    }

    @Override
    public SocketResponseOprs setBoolean(int index, boolean value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setBoolean(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setByte(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE, "value: %s", value);
        byteBuf.setByte(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedByte(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Byte.MAX_VALUE - Byte.MIN_VALUE, "value: %s", value);
        byteBuf.setByte(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setShort(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE, "value: %s", value);
        byteBuf.setShort(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedShort(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= 0 && value <= Short.MAX_VALUE - Short.MIN_VALUE, "value: %s", value);
        byteBuf.setShort(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setInt(int index, int value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setInt(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setUnsignedInt(int index, long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setInt(index, (int) value);
        return this;
    }

    @Override
    public SocketResponseOprs setLong(int index, long value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        byteBuf.setLong(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setFloat(int index, float value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Float.MIN_VALUE && value <= Float.MAX_VALUE, "value: %s", value);
        byteBuf.setFloat(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setDouble(int index, double value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value >= Double.MIN_VALUE && value <= Double.MAX_VALUE, "value: %s", value);
        byteBuf.setDouble(index, value);
        return this;
    }

    @Override
    public SocketResponseOprs setBytes(int index, byte[] value) {
        Preconditions.checkArgument(mode == WRITE_MODE);
        Preconditions.checkArgument(value != null);
        byteBuf.setBytes(index, value);
        return this;
    }

    //------------------------------------------var int/long reader 算法来自于protocolbuf------------------------------------------
    private int readRawVarInt32() {
        return decodeZigZag32(_readRawVarInt32());
    }

    /**
     * Decode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be
     * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
     * to be varint encoded, thus always taking 10 bytes on the wire.)
     *
     * @param n An unsigned 32-bit integer, stored in a signed int because Java has no explicit
     *          unsigned support.
     * @return A signed 32-bit integer.
     */
    private int decodeZigZag32(int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * read 变长 32位int
     */
    private int _readRawVarInt32() {
        fastpath:
        {
            int readerIndex = byteBuf.readerIndex();

            if (byteBuf.readableBytes() <= 0) {
                break fastpath;
            }

            int x;
            if ((x = byteBuf.readByte()) >= 0) {
                return x;
            } else if (byteBuf.readableBytes() < 9) {
                //reset reader index
                byteBuf.readerIndex(readerIndex);
                break fastpath;
            } else if ((x ^= (byteBuf.readByte() << 7)) < 0) {
                x ^= (~0 << 7);
            } else if ((x ^= (byteBuf.readByte() << 14)) >= 0) {
                x ^= (~0 << 7) ^ (~0 << 14);
            } else if ((x ^= (byteBuf.readByte() << 21)) < 0) {
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
            } else {
                int y = byteBuf.readByte();
                x ^= y << 28;
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
                if (y < 0
                        && byteBuf.readByte() < 0
                        && byteBuf.readByte() < 0
                        && byteBuf.readByte() < 0
                        && byteBuf.readByte() < 0
                        && byteBuf.readByte() < 0) {
                    //reset reader index
                    byteBuf.readerIndex(readerIndex);
                    break fastpath; // Will throw malformedVarint()
                }
            }
            return x;
        }
        return (int) readRawVarint64SlowPath();
    }

    private long readRawVarint64SlowPath() {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = readRawByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new IllegalArgumentException("encountered a malformed varint");
    }

    private long readRawVarLong64() {
        return decodeZigZag64(_readRawVarLong64());
    }

    /**
     * Decode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be
     * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
     * to be varint encoded, thus always taking 10 bytes on the wire.)
     *
     * @param n An unsigned 64-bit integer, stored in a signed int because Java has no explicit
     *          unsigned support.
     * @return A signed 64-bit integer.
     */
    public long decodeZigZag64(long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * read 变长 64位long
     */
    public long _readRawVarLong64() {
        // Implementation notes:
        //
        // Optimized for one-byte values, expected to be common.
        // The particular code below was selected from various candidates
        // empirically, by winning VarintBenchmark.
        //
        // Sign extension of (signed) Java bytes is usually a nuisance, but
        // we exploit it here to more easily obtain the sign of bytes read.
        // Instead of cleaning up the sign extension bits by masking eagerly,
        // we delay until we find the final (positive) byte, when we clear all
        // accumulated bits with one xor.  We depend on javac to constant fold.
        fastpath:
        {
            int readerIndex = byteBuf.readerIndex();

            if (byteBuf.readableBytes() <= 0) {
                break fastpath;
            }

            long x;
            int y;
            if ((y = byteBuf.readByte()) >= 0) {
                return y;
            } else if (byteBuf.readableBytes() < 9) {
                //reset reader index
                byteBuf.readerIndex(readerIndex);
                break fastpath;
            } else if ((y ^= (byteBuf.readByte() << 7)) < 0) {
                x = y ^ (~0 << 7);
            } else if ((y ^= (byteBuf.readByte() << 14)) >= 0) {
                x = y ^ ((~0 << 7) ^ (~0 << 14));
            } else if ((y ^= (byteBuf.readByte() << 21)) < 0) {
                x = y ^ ((~0 << 7) ^ (~0 << 14) ^ (~0 << 21));
            } else if ((x = y ^ ((long) byteBuf.readByte() << 28)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28);
            } else if ((x ^= ((long) byteBuf.readByte() << 35)) < 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35);
            } else if ((x ^= ((long) byteBuf.readByte() << 42)) >= 0L) {
                x ^= (~0L << 7) ^ (~0L << 14) ^ (~0L << 21) ^ (~0L << 28) ^ (~0L << 35) ^ (~0L << 42);
            } else if ((x ^= ((long) byteBuf.readByte() << 49)) < 0L) {
                x ^=
                        (~0L << 7)
                                ^ (~0L << 14)
                                ^ (~0L << 21)
                                ^ (~0L << 28)
                                ^ (~0L << 35)
                                ^ (~0L << 42)
                                ^ (~0L << 49);
            } else {
                x ^= ((long) byteBuf.readByte() << 56);
                x ^=
                        (~0L << 7)
                                ^ (~0L << 14)
                                ^ (~0L << 21)
                                ^ (~0L << 28)
                                ^ (~0L << 35)
                                ^ (~0L << 42)
                                ^ (~0L << 49)
                                ^ (~0L << 56);
                if (x < 0L) {
                    if (byteBuf.readByte() < 0L) {
                        //reset reader index
                        byteBuf.readerIndex(readerIndex);
                        break fastpath; // Will throw malformedVarint()
                    }
                }
            }
            return x;
        }
        return readRawVarint64SlowPath();
    }

    private byte readRawByte() {
        if (byteBuf.readableBytes() <= 0) {
            throw new IllegalStateException("While parsing a protocol, the input ended unexpectedly "
                    + "in the middle of a field.  This could mean either that the "
                    + "input has been truncated or that an embedded message "
                    + "misreported its own length.");
        }
        return byteBuf.readByte();
    }

    //------------------------------------------var int/long writer 算法来自于protocolbuf------------------------------------------
    private void writeRawVarInt32(int value) {
        _writeRawVarInt32(encodeZigZag32(value));
    }

    /**
     * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be
     * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
     * to be varint encoded, thus always taking 10 bytes on the wire.)
     *
     * @param n A signed 32-bit integer.
     * @return An unsigned 32-bit integer, stored in a signed int because Java has no explicit
     * unsigned support.
     */
    private int encodeZigZag32(int n) {
        // Note:  the right-shift must be arithmetic
        return (n << 1) ^ (n >> 31);
    }

    private void _writeRawVarInt32(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                byteBuf.writeByte(value);
                return;
            } else {
                byteBuf.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    private void writeRawVarlong64(long value) {
        _writRawVarLong64(encodeZigZag64(value));
    }

    /**
     * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be
     * efficiently encoded with varint. (Otherwise, negative values must be sign-extended to 64 bits
     * to be varint encoded, thus always taking 10 bytes on the wire.)
     *
     * @param n A signed 64-bit integer.
     * @return An unsigned 64-bit integer, stored in a signed int because Java has no explicit
     * unsigned support.
     */
    private long encodeZigZag64(long n) {
        // Note:  the right-shift must be arithmetic
        return (n << 1) ^ (n >> 63);
    }

    private void _writRawVarLong64(long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                byteBuf.writeByte((int) value);
                return;
            } else {
                byteBuf.writeByte(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------
}
