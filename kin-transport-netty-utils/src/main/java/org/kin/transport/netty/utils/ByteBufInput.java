package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.Input;

import java.util.Objects;

/**
 * 基于{@link ByteBuf}的{@link Input}实现
 *
 * @author huangjianqin
 * @date 2021/12/15
 */
public class ByteBufInput implements Input {
    private final ByteBuf byteBuf;

    public ByteBufInput(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public byte readByte() {
        return byteBuf.readByte();
    }

    @Override
    public Input readBytes(byte[] dst, int dstIndex, int length) {
        if (Objects.isNull(dst)) {
            throw new IllegalArgumentException("dst is null");
        }
        if (dstIndex < 0) {
            throw new IndexOutOfBoundsException("dstIndex < 0");
        }
        if (readableBytes() < length) {
            throw new IndexOutOfBoundsException("length is greater than readableBytes");
        }
        byteBuf.readBytes(dst, dstIndex, length);
        return this;
    }

    @Override
    public int readerIndex() {
        return byteBuf.readerIndex();
    }

    @Override
    public Input readerIndex(int readerIndex) {
        if (readerIndex < 0) {
            throw new IndexOutOfBoundsException("readerIndex < 0");
        }
        byteBuf.readerIndex(readerIndex);
        return this;
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
