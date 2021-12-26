package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import org.kin.framework.io.Output;

/**
 * 基于{@link ByteBuf}的{@link Output}实现
 *
 * @author huangjianqin
 * @date 2021/12/15
 */
public class ByteBufOutput implements Output {
    private final ByteBuf byteBuf;

    public ByteBufOutput(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public void writeByte(int value) {
        byteBuf.writeByte(value);
    }

    @Override
    public void writeBytes(byte[] value, int startIdx, int len) {
        byteBuf.writeBytes(value, startIdx, len);
    }

    @Override
    public int writableBytes() {
        //认为是可无限写入, 因为ByteBuf会自动扩容
        return Integer.MAX_VALUE;
    }
}
