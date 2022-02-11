package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.kin.framework.utils.ByteUnit;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangjianqin
 * @date 2022/2/11
 */
public class AdaptiveByteBufAllocatorTest {
    public static void main(String[] args) {
        AdaptiveByteBufAllocator.Handle handle = AdaptiveByteBufAllocator.DEFAULT.newHandle();
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;

        //1KB
        int bound = 1024;
        for (int i = 0; i < 100; i++) {
            ByteBuf byteBuf = handle.allocate(allocator);
            int size = ThreadLocalRandom.current().nextInt(bound);
            System.out.println(String.format("%d: size:%s, capacity:%s", i, ByteUnit.format(size), ByteUnit.format(byteBuf.writableBytes())));
            for (int j = 0; j < size / 8; j++) {
                byteBuf.writeLong(1);
            }
            handle.record(byteBuf.readableBytes());
        }
    }
}
