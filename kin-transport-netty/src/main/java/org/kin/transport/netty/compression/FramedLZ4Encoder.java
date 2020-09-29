package org.kin.transport.netty.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * FramedLZ4 compressor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class FramedLZ4Encoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream baso = new ByteArrayOutputStream();
        FramedLZ4CompressorOutputStream framedLZ4Outputstream = new FramedLZ4CompressorOutputStream(baso);
        try {
            msg.readBytes(framedLZ4Outputstream, msg.readableBytes());
            framedLZ4Outputstream.finish();
            framedLZ4Outputstream.flush();
        } finally {
            framedLZ4Outputstream.close();
            baso.close();
        }
        out.writeBytes(baso.toByteArray());
    }
}