package org.kin.transport.netty.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * bzip2 compressor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class BZip2Encoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream baso = new ByteArrayOutputStream();
        BZip2CompressorOutputStream bzip2Outputstream = new BZip2CompressorOutputStream(baso);
        try {
            msg.readBytes(bzip2Outputstream, msg.readableBytes());
            bzip2Outputstream.finish();
            bzip2Outputstream.flush();
        } finally {
            bzip2Outputstream.close();
            baso.close();
        }
        out.writeBytes(baso.toByteArray());
    }
}
