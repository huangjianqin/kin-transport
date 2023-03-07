package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;

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
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof ByteBufPayload) {
                ByteBufPayload byteBufPayload = (ByteBufPayload) msg;

                ByteBuf byteBuf = byteBufPayload.getData();
                //当前可读字节数, 包含header
                int length = byteBuf.readableBytes();

                //记录当前write index
                byteBuf.markWriterIndex();
                //重置到header
                byteBuf.writerIndex(byteBuf.writerIndex() - length);
                //write protocol content
                byteBuf.writeInt(length - Protocols.PROTOCOL_LENGTH_BYTES)
                        //write header
                        .writeBytes(options.getMagic());
                //回滚到之前的write index
                byteBuf.resetWriterIndex();

                ctx.write(byteBuf, promise);

                if (byteBufPayload instanceof OutboundPayload) {
                    //记录此次response的bytes大小, 以便下次一次性分配足够大的ByteBuf, 减少copy次数
                    ((OutboundPayload) byteBufPayload).recordPayloadSize();
                }
            } else {
                ctx.write(msg, promise);
            }
        } catch (Throwable t) {
            throw new EncoderException(t);
        }
    }
}
