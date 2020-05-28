package org.kin.transport.netty.socket.handler;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.ReferenceCountUtil;
import org.kin.transport.netty.core.domain.GlobalRatelimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2019/5/29
 * 主要是校验协议头
 */
public class ByteFrameCodec extends ByteToMessageCodec<ByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(ByteFrameCodec.class);

    private static final byte[] FRAME_MAGIC = "kin-transport".getBytes();
    /** 协议帧长度字段占位大小 */
    private final int FRAME_BODY_SIZE = 4;
    private final int MAX_BODY_SIZE;
    /** true = in, false = out */
    private final boolean serverElseClient;
    private final int FRAME_BASE_LENGTH;

    private final RateLimiter globalRateLimiter;

    public ByteFrameCodec(int maxBodySize, boolean serverElseClient, int globalRateLimit) {
        this.MAX_BODY_SIZE = maxBodySize;
        this.serverElseClient = serverElseClient;
        this.FRAME_BASE_LENGTH = FRAME_MAGIC.length + FRAME_BODY_SIZE;
        if (globalRateLimit > 0) {
            globalRateLimiter = RateLimiter.create(globalRateLimit);
        } else {
            globalRateLimiter = null;
        }
    }

    public static ByteFrameCodec clientFrameCodec() {
        return new ByteFrameCodec(1024000, false, 0);
    }

    public static ByteFrameCodec serverFrameCodec() {
        return serverFrameCodec(0);
    }

    public static ByteFrameCodec serverFrameCodec(int globalRateLimit) {
        return new ByteFrameCodec(1024000, true, globalRateLimit);
    }

    private boolean isMagicRight(byte[] magic) {
        return Arrays.equals(magic, FRAME_MAGIC);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        try {
            if (!serverElseClient) {
                out.writeBytes(FRAME_MAGIC);
            }
            int bodySize = in.readableBytes();
            out.writeInt(bodySize);
            out.writeBytes(in, bodySize);
        } finally {
            ReferenceCountUtil.release(in);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            if (Objects.nonNull(globalRateLimiter) && !globalRateLimiter.tryAcquire()) {
                //全局流控
                ctx.fireUserEventTriggered(GlobalRatelimitEvent.INSTANCE);
                return;
            }
            //解决拆包问题
            in.markReaderIndex();
            //合并解包
            int bodySize;
            if (serverElseClient) {
                if (in.readableBytes() < FRAME_BASE_LENGTH) {
                    return;
                }

                byte[] magic = new byte[FRAME_MAGIC.length];
                in.readBytes(magic);

                //校验魔数
                if (!isMagicRight(magic)) {
                    String hexDump = ByteBufUtil.hexDump(in);
                    //校验不通过, 直接清空inbound, 存在丢包的可能, 保证client存在重试机制
                    in.skipBytes(in.readableBytes());
                    throw new CorruptedFrameException(String.format("FrameHeaderError: magic=%s, HexDump=%s", Arrays.toString(magic), hexDump));
                }

                bodySize = in.readInt();

                if (bodySize > MAX_BODY_SIZE) {
                    //校验不通过, 直接清空inbound, 存在丢包的可能, 保证client存在重试机制
                    in.skipBytes(in.readableBytes());
                    throw new IllegalStateException(String.format("BodySize[%s] too large!", bodySize));
                }
            } else {
                if (in.readableBytes() < FRAME_BODY_SIZE) {
                    return;
                }

                bodySize = in.readInt();
            }

            int bodyReadableSize = in.readableBytes();
            if (bodyReadableSize < bodySize) {
                //解决拆包, 等待下次数据帧补满
                in.resetReaderIndex();
                return;
            }

            ByteBuf frameBuf = ctx.alloc().buffer(bodySize);
            frameBuf.writeBytes(in, bodySize);
            ReferenceCountUtil.retain(frameBuf);
            out.add(frameBuf);
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}
