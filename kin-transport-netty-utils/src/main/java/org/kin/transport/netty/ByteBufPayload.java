package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.Recycler;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public final class ByteBufPayload extends AbstractReferenceCounted {
    /** {@link ByteBufPayload}实例对象池 */
    private static final Recycler<ByteBufPayload> RECYCLER =
            new Recycler<ByteBufPayload>() {
                protected ByteBufPayload newObject(Handle<ByteBufPayload> handle) {
                    return new ByteBufPayload(handle);
                }
            };
    /** 回收句柄 */
    private final Recycler.Handle<ByteBufPayload> handle;
    /**
     * 用于outbound, 主要为了反馈最终协议内容大小, 以支持自适应分配outbound bytebuf大小,
     * 减少组装协议时, 多次扩容而带来的bytebuf copy性能开销
     */
    private AdaptiveOutputByteBufAllocator.Handle adaptiveHandle;

    /** 持有{@link ByteBuf}实例 */
    private ByteBuf payload;

    private ByteBufPayload(final Recycler.Handle<ByteBufPayload> handle) {
        this.handle = handle;
    }

    /**
     * 一般用于inbound
     * <p>
     * {@code byteBuf}refCnt一般是2, 因为netty pipeline handler处理后会对inbound buffer进行release
     * 所以decode完后, 会payload进行retain, 后续handler才行正常执行
     *
     * @param byteBuf byte buffer
     * @return {@link ByteBufPayload}实例
     */
    public static ByteBufPayload create(ByteBuf byteBuf) {
        return create(byteBuf, null);
    }

    /**
     * 一般用于outbound
     *
     * @param byteBuf byte buffer
     * @return {@link ByteBufPayload}实例
     */
    public static ByteBufPayload create(ByteBuf byteBuf, AdaptiveOutputByteBufAllocator.Handle adaptiveHandle) {
        ByteBufPayload payload = RECYCLER.get();
        payload.payload = byteBuf;
        payload.adaptiveHandle = adaptiveHandle;
        // ensure byteBuf and metadata is set before refCnt change
        payload.setRefCnt(1);
        return payload;
    }

    @Override
    public ByteBufPayload retain() {
        super.retain();
        return this;
    }

    @Override
    public ByteBufPayload retain(int increment) {
        super.retain(increment);
        return this;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void deallocate() {
        if (Objects.nonNull(adaptiveHandle)) {
            //反馈response payload大小, 以供自适应分配器调整下次直接分配bytebuf的大小
            adaptiveHandle.record(payload.writerIndex());
        }
        payload.release();
        //recycle
        handle.recycle(this);

        //help gc
        payload = null;
        adaptiveHandle = null;
    }

    @Override
    public ByteBufPayload touch() {
        ensureAccessible();
        payload.touch();
        return this;
    }

    @Override
    public ByteBufPayload touch(Object o) {
        ensureAccessible();
        payload.touch(o);
        return this;
    }

    /**
     * 访问bytebuf前都应该调用, 确保是bytebuf还没被release
     */
    private void ensureAccessible() {
        if (!isAccessible()) {
            throw new IllegalReferenceCountException(0);
        }
    }

    /**
     * 判断bytebuf是否被release
     */
    private boolean isAccessible() {
        return refCnt() != 0;
    }

    /**
     * 获取bytebuf
     */
    public ByteBuf data() {
        ensureAccessible();
        return payload;
    }
}
