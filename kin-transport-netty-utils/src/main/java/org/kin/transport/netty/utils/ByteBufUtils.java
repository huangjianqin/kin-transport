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
     * 修正{@link ByteBuf#readerIndex()}
     * 因为底层使用{@link ByteBuf#nioBuffer()}获取{@link java.nio.ByteBuffer}实例,
     * 但修改{@link java.nio.ByteBuffer}实例, 对{@link io.netty.buffer.ByteBuf}不可见,
     * 故完成output后需要修正{@link ByteBuf#readerIndex()}
     *
     * @param byteBuf   netty byte buffer
     * @param nioBuffer {@code byteBuf}底层映射的java byte buffer
     */
    public static void fixByteBufReadIndex(ByteBuf byteBuf, ByteBuffer nioBuffer) {
        int actualReadBytes = byteBuf.readerIndex();
        if (nioBuffer != null) {
            actualReadBytes += nioBuffer.position();
        }

        byteBuf.readerIndex(actualReadBytes);
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
        //这里需要position + minWritableBytes, 是因为改动底层nioBuffer, 上层byteBuf writer index并不会发生变化,
        //加上position, 才能反应真实writer index
        nioBuffer = newNioByteBuffer(byteBuf, position + minWritableBytes);
        //回到之前write index
        nioBuffer.position(position);
        return nioBuffer;
    }

    /**
     * 创建{@link ByteBuf}底层内存映射的满足可写字节数{@code minWritableBytes}的{@link ByteBuffer}实例
     *
     * @param byteBuf          netty byte buffer
     * @param minWritableBytes 可写字节数
     */
    private static ByteBuffer newNioByteBuffer(ByteBuf byteBuf, int minWritableBytes) {
        return byteBuf
                //扩容, 新容量可能>=minWritableBytes, 因为内部会round to power2
                .ensureWritable(minWritableBytes)
                //取底层ByteBuffer, 因为操作底层ByteBuffer时, 不会影响包装ByteBuf的writerIndex, 这里使用原来的writerIndex, 保证获取到的底层ByteBuffer与扩容前的position值一致(仅仅容量变大了)
                .nioBuffer(byteBuf.writerIndex(), byteBuf.writableBytes());
    }
}
