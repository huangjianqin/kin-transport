package org.kin.transport.netty.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * FramedLZ4 decompressor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class FramedLZ4Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        FramedLZ4CompressorInputStream framedLZ4InputStream = new FramedLZ4CompressorInputStream(bais);
        try {
            ByteBuf outByteBuf = ctx.alloc().buffer();
            byte[] readBuf = new byte[1024];
            //read bytes length
            int readNum;
            while ((readNum = framedLZ4InputStream.read(readBuf)) > 0) {
                outByteBuf.writeBytes(readBuf, 0, readNum);
            }
            out.add(outByteBuf);
        } finally {
            framedLZ4InputStream.close();
            bais.close();
        }
    }
}
