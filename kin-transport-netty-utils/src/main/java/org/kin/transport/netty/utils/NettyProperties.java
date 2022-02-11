package org.kin.transport.netty.utils;

/**
 * 支持的system property
 *
 * @author huangjianqin
 * @date 2021/12/8
 */
public interface NettyProperties {
    /**
     * the number of flushes after which an explicit flush will be done
     * flush操作后多少次flush才是真正一次flush
     */
    String KIN_TRANSPORT_NETTY_EXPLICIT_FLUSH_AFTER_FLUSHES = "kin.transport.netty.explicitFlushAfterFlushes";
    /** 是否启动自定义空闲检查handler, 在连接数较大场景, 提高空闲检查性能, 默认使用netty内置的空闲检查handler */
    String KIN_TRANSPORT_NETTY_IDLE_STATE_HANDLER = "kin.transport.netty.idleStateHandler";
}
