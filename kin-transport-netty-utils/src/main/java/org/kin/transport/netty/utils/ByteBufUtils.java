package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * @author huangjianqin
 * @date 2021/11/28
 */
public final class ByteBufUtils {
    private ByteBufUtils() {
    }

    /**
     * 将{@link ByteBuffer}转换为bytes
     */
    public static byte[] toBytes(ByteBuf target) {
        byte[] bytes = new byte[target.readableBytes()];
        target.readBytes(bytes);
        return bytes;
    }

    /**
     * 修正{@link ByteBuf#writerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#writerIndex()}
     *
     * @param byteBuf   netty byte buffer
     * @param nioBuffer {@code byteBuf}底层映射的java byte buffer
     */
    public static void fixByteBufWriteIndex(ByteBuf byteBuf, ByteBuffer nioBuffer) {
        int actualWroteBytes = byteBuf.writerIndex();
        if (nioBuffer != null) {
            actualWroteBytes += nioBuffer.position();
        }

        byteBuf.writerIndex(actualWroteBytes);
    }

    /**
     * 创建{@link ByteBuf}底层内存映射的满足最小可写字节数{@code minWritableBytes}的{@link ByteBuffer}实例
     *
     * @param byteBuf          netty byte buffer
     * @param nioBuffer        当前{@code byteBuf}底层映射的java byte buffer
     * @param minWritableBytes 最小可写字节数
     */
    public static ByteBuffer nioBuffer(ByteBuf byteBuf, ByteBuffer nioBuffer, int minWritableBytes) {
        if (minWritableBytes < 0) {
            minWritableBytes = byteBuf.writableBytes();
        }

        if (nioBuffer == null) {
            nioBuffer = newNioByteBuffer(byteBuf, minWritableBytes);
        }

        if (nioBuffer.remaining() >= minWritableBytes) {
            return nioBuffer;
        }

        int position = nioBuffer.position();
        nioBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
        //回到之前write index
        nioBuffer.position(position);
        return nioBuffer;
    }

    /**
     * 创建{@link ByteBuf}底层内存映射的满足可写字节数{@code writableBytes}的{@link ByteBuffer}实例
     *
     * @param byteBuf       netty byte buffer
     * @param writableBytes 可写字节数
     */
    private static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int writableBytes) {
        return byteBuf
                .ensureWritable(writableBytes)
                .nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
}
