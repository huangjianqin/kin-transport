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
                int length = byteBuf.readableBytes();

                //记录当前write index
                byteBuf.markWriterIndex();
                //重置到header
                byteBuf.writerIndex(byteBuf.writerIndex() - length);
                //write protocol content
                byteBuf.writeInt(length - Protocols.PROTOCOL_LENGTH_MARK_BYTES)
                        //write header
                        .writeBytes(options.getMagic());
                //回滚到之前的write index
                byteBuf.resetWriterIndex();

                ctx.write(byteBuf, promise);
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
