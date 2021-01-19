package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 支持缓存channel消息的protocol handler, 用于支持批量提交task给业务线程池的场景
 *
 * @author huangjianqin
 * @date 2021/1/19
 */
public abstract class CachedProtocolHandler<MSG> extends ProtocolHandler<MSG> {
    /** 缓存channel消息队列的attribute key */
    private final AttributeKey<LinkedList<MSG>> queueAttrKey = AttributeKey.newInstance("kinrpc-message-queue$".concat(getClass().getSimpleName()));
    /** 缓存存活时间 */
    private final int cacheTtl;
    /** 缓存存活时间单位 */
    private final TimeUnit ttlTimeUnit;

    public CachedProtocolHandler(int cacheTtl, TimeUnit ttlTimeUnit) {
        this.cacheTtl = cacheTtl;
        this.ttlTimeUnit = ttlTimeUnit;

        Preconditions.checkArgument(cacheTtl > 0, "channel message ttl must be greater than 0");
    }

    @Override
    public final void handle(ChannelHandlerContext ctx, MSG protocol) {
        Channel channel = ctx.channel();
        Attribute<LinkedList<MSG>> attribute = channel.attr(queueAttrKey);
        LinkedList<MSG> queue = attribute.get();
        if (Objects.isNull(queue)) {
            queue = new LinkedList<>();
            attribute.set(queue);
            channel.eventLoop().schedule(() -> batchHandle(ctx), cacheTtl, ttlTimeUnit);
        }
        //缓存channel消息
        queue.add(protocol);
    }

    /**
     * 在channel线程调用
     * 批量处理消息
     */
    private void batchHandle(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Attribute<LinkedList<MSG>> attribute = channel.attr(queueAttrKey);
        LinkedList<MSG> queue = attribute.get();
        attribute.set(null);

        batchHandle(ctx, queue);
    }

    /**
     * 处理channel缓存的协议
     * 在channel线程调用
     *
     * @param ctx       channel上下文
     * @param protocols 缓存的协议
     */
    protected abstract void batchHandle(ChannelHandlerContext ctx, LinkedList<MSG> protocols);
}
