package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.kin.transport.netty.CompressionType;

/**
 * out -> 根据指定类型对数据源进行压缩
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
class SocketCompressor extends ChannelOutboundHandlerAdapter {
    /** 压缩类型 */
    private final CompressionType compressionType;

    public SocketCompressor(CompressionType compressionType) {
        this.compressionType = compressionType;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            return;
        }
        ByteBuf out = ctx.alloc().buffer();
        out.writeByte(compressionType.getId());
        out.writeBytes((ByteBuf) msg);

        ReferenceCountUtil.retain(out);
        ctx.write(out, promise);
    }
}
