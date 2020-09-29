package org.kin.transport.netty.socket;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import org.kin.transport.netty.CompressionType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * in -> 根据请求的压缩类型对数据进行解压缩
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class SocketDecompressor extends SimpleChannelInboundHandler<ByteBuf> {
    /** decoder缓存, 软引用, 30min空闲移除 */
    private Cache<CompressionType, ByteToMessageDecoder> decoderCache =
            CacheBuilder.newBuilder()
                    .softValues()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .build();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //压缩类型
        int compressionId = in.readByte();
        CompressionType compressionType = CompressionType.getByName(compressionId);
        if (Objects.isNull(compressionType)) {
            return;
        }

        ReferenceCountUtil.retain(in);


        if (!CompressionType.NONE.equals(compressionType)) {
            ByteToMessageDecoder decoder = decoderCache.get(compressionType, compressionType::decoder);
            decoder.channelRead(ctx, in);
        } else {
            ctx.fireChannelRead(in);
        }
    }
}
