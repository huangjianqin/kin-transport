package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.utils.AdaptiveOutputByteBufAllocator;

/**
 * 用于outbound, 主要为了反馈最终协议内容大小, 以支持自适应分配outbound bytebuf大小,
 * 减少组装协议时, 多次扩容而带来的bytebuf copy性能开销
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public class OutboundPayload extends ByteBufPayload {
    private final AdaptiveOutputByteBufAllocator.Handle adaptiveHandle;

    public OutboundPayload(AdaptiveOutputByteBufAllocator.Handle adaptiveHandle, ByteBuf data) {
        super(data);
        this.adaptiveHandle = adaptiveHandle;
    }

    /**
     * 反馈response payload大小, 以供自适应分配器调整下次直接分配bytebuf的大小
     */
    public void recordPayloadSize() {
        adaptiveHandle.record(data.writerIndex());
    }
}
