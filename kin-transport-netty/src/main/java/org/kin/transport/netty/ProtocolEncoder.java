package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
@ChannelHandler.Sharable
public class ProtocolEncoder extends ChannelOutboundHandlerAdapter {
    /** 传输层配置 */
    private final ProtocolOptions options;

    public ProtocolEncoder(ProtocolOptions options) {
        this.options = options;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof ByteBufPayload) {
            try {
                ByteBufPayload payload = (ByteBufPayload) msg;

                ByteBuf byteBuf = payload.data();
                //当前可读字节数, 包含header
                int readableBytes = byteBuf.readableBytes();

                //记录当前write index
                byteBuf.markWriterIndex();
                //重置到header
                byteBuf.writerIndex(byteBuf.writerIndex() - readableBytes);
                //write header
                //write magic
                byteBuf.writeBytes(options.getMagic())
                        //write body size
                        .writeInt(readableBytes - options.getHeaderSize());
                //回滚到之前的write index
                byteBuf.resetWriterIndex();

                //cte write完成会对bytebuf进行release一次
                ctx.write(byteBuf.retain(), promise);
            } catch (Throwable t) {
                throw new EncoderException(t);
            } finally {
                ReferenceCountUtil.safeRelease((msg));
            }
        } else {
            ctx.write(msg, promise);
        }
    }
}
