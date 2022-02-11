package org.kin.transport.netty.utils;

/**
 * 自定义的system property
 *
 * @author huangjianqin
 * @date 2021/12/8
 */
public interface NettySysProperties {
    /**
     * the number of flushes after which an explicit flush will be done
     * flush操作后多少次flush才是真正一次flush
     */
    String KIN_NETTY_EXPLICIT_FLUSH_AFTER_FLUSHES = "kin.netty.explicitFlushAfterFlushes";
    /** 是否启动自定义空闲检查handler, 在连接数较大场景, 提高空闲检查性能, 默认使用netty内置的空闲检查handler */
    String KIN_NETTY_IDLE_STATE_HANDLER = "kin.netty.idleStateHandler";
    /** size表double增长阈值, 超过阈值的size会以double增长, 阈值前的size设置参考{@link io.netty.buffer.SizeClasses} */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_THRESHOLD = "kin.netty.adaptiveByteBufAllocator.threshold";
    /** 预测下次分配字节满足增长前提时, index增加n, 下次分配更多字节数, 当然不会超过max */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_INCREMENT = "kin.netty.adaptiveByteBufAllocator.indexIncrement";
    /** 预测下次分配字节满足减少前提时, index减少n, 下次分配更少字节数, 当然不会低于min */
    String KIN_NETTY_ADAPTIVE_ALLOCATOR_INDEX_DECREMENT = "kin.netty.adaptiveByteBufAllocator.indexDecrement";
}
