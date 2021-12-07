package org.kin.transport.netty.estimator;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.FileRegion;
import io.netty.channel.MessageSizeEstimator;

/**
 * 消息size计算, 努力反应真实的IO水位线
 *
 * @author huangjianqin
 * @date 2021/12/7
 */
public final class MessageSizeEstimatorImpl implements MessageSizeEstimator {
    /** 默认实现, 默认8直接大小 */
    public static final MessageSizeEstimator INSTANCE = new MessageSizeEstimatorImpl(8);

    /**
     * 默认实现
     */
    private static final class HandleImpl implements Handle {
        /** 默认消息大小 */
        private final int unknownSize;

        private HandleImpl(int unknownSize) {
            this.unknownSize = unknownSize;
        }

        @Override
        public int size(Object msg) {
            if (msg instanceof ByteBuf) {
                return ((ByteBuf) msg).readableBytes();
            }

            if (msg instanceof ByteBufHolder) {
                return ((ByteBufHolder) msg).content().readableBytes();
            }

            if (msg instanceof FileRegion) {
                return 0;
            }

            if (msg instanceof MessageSize) {
                return ((MessageSize) msg).size();
            }

            return unknownSize;
        }
    }

    /** {@link MessageSizeEstimator.Handle}实现 */
    private final Handle handle;

    public MessageSizeEstimatorImpl(int unknownSize) {
        if (unknownSize < 0) {
            throw new IllegalArgumentException("unknownSize: " + unknownSize + " (expected: >= 0)");
        }
        handle = new HandleImpl(unknownSize);
    }

    @Override
    public Handle newHandle() {
        return handle;
    }
}
