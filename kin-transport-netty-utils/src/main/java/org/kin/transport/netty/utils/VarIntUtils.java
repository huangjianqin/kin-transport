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

    //------------------------------------------var int/long reader 算法来自于protocolbuf------------------------------------------
    public static int readRawVarInt32(ByteBuf byteBuf) {
        return readRawVarInt32(byteBuf, true);
    }

    public static int readRawVarInt32(ByteBuf byteBuf, boolean zigzag) {
        int rawVarInt32 = _readRawVarInt32(byteBuf);
        if (zigzag) {
            return org.kin.framework.utils.VarIntUtils.decodeZigZag32(rawVarInt32);
        } else {
            return rawVarInt32;
        }
    }

    /**
     * read 变长 32位int
     */
    private static int _readRawVarInt32(ByteBuf byteBuf) {
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
        return (int) readRawVarInt64SlowPath(byteBuf);
    }

    private static long readRawVarInt64SlowPath(ByteBuf byteBuf) {
        long result = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            final byte b = readRawByte(byteBuf);
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
        }
        throw new IllegalArgumentException("encountered a malformed varint");
    }

    public static long readRawVarLong64(ByteBuf byteBuf) {
        return readRawVarLong64(byteBuf, true);
    }

    public static long readRawVarLong64(ByteBuf byteBuf, boolean zigzag) {
        long rawVarLong64 = _readRawVarLong64(byteBuf);
        if (zigzag) {
            return org.kin.framework.utils.VarIntUtils.decodeZigZag64(rawVarLong64);
        } else {
            return rawVarLong64;
        }
    }

    /**
     * read 变长 64位long
     */
    private static long _readRawVarLong64(ByteBuf byteBuf) {
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
        return readRawVarInt64SlowPath(byteBuf);
    }

    private static byte readRawByte(ByteBuf byteBuf) {
        if (byteBuf.readableBytes() <= 0) {
            throw new IllegalStateException("While parsing a protocol, the input ended unexpectedly "
                    + "in the middle of a field.  This could mean either that the "
                    + "input has been truncated or that an embedded message "
                    + "misreported its own length.");
        }
        return byteBuf.readByte();
    }

    //------------------------------------------var int/long writer 算法来自于protocolbuf------------------------------------------
    public static void writeRawVarInt32(ByteBuf byteBuf, int value) {
        writeRawVarInt32(byteBuf, value, true);
    }

    public static void writeRawVarInt32(ByteBuf byteBuf, int value, boolean zigzag) {
        if (zigzag) {
            value = org.kin.framework.utils.VarIntUtils.encodeZigZag32(value);
        }
        _writeRawVarInt32(byteBuf, value);
    }

    private static void _writeRawVarInt32(ByteBuf byteBuf, int value) {
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

    public static void writeRawVarLong64(ByteBuf byteBuf, long value) {
        writeRawVarLong64(byteBuf, value, true);
    }

    public static void writeRawVarLong64(ByteBuf byteBuf, long value, boolean zigzag) {
        if (zigzag) {
            value = org.kin.framework.utils.VarIntUtils.decodeZigZag64(value);
        }
        _writRawVarLong64(byteBuf, value);
    }

    private static void _writRawVarLong64(ByteBuf byteBuf, long value) {
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
