package org.kin.transport.netty.utils;

/**
 * 自定义的system property
 *
 * @author huangjianqin
 * @date 2021/12/8
 */
public interface NettyProperties {
    /** size表double增长阈值, 超过阈值的size会以double增长, 阈值前的size设置参考{@link io.netty.buffer.SizeClasses}, 建议设置的阈值大于大部分消息大小 */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_THRESHOLD = "kin.netty.adaptiveByteBufAllocator.threshold";
    /** 预测下次分配字节满足增长前提时, index增加n, 下次分配更多字节数, 当然不会超过max */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_INCREMENT = "kin.netty.adaptiveByteBufAllocator.indexIncrement";
    /** 预测下次分配字节满足减少前提时, index减少n, 下次分配更少字节数, 当然不会低于min */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_DECREMENT = "kin.netty.adaptiveByteBufAllocator.indexDecrement";
}
