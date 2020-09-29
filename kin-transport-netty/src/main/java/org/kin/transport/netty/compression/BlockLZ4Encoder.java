package org.kin.transport.netty.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * BlockLZ4 compressor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class BlockLZ4Encoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream baso = new ByteArrayOutputStream();
        BlockLZ4CompressorOutputStream blockLZ4Outputstream = new BlockLZ4CompressorOutputStream(baso);
        try {
            msg.readBytes(blockLZ4Outputstream, msg.readableBytes());
            blockLZ4Outputstream.finish();
            blockLZ4Outputstream.flush();
        } finally {
            blockLZ4Outputstream.close();
            baso.close();
        }
        out.writeBytes(baso.toByteArray());
    }
}
