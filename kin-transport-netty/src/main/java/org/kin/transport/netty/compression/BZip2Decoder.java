package org.kin.transport.netty.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * bzip2 decompressor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
public class BZip2Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BZip2CompressorInputStream bzip2InputStream = new BZip2CompressorInputStream(bais);
        try {
            ByteBuf outByteBuf = ctx.alloc().buffer();
            byte[] readBuf = new byte[1024];
            //read bytes length
            int readNum;
            while ((readNum = bzip2InputStream.read(readBuf)) > 0) {
                outByteBuf.writeBytes(readBuf, 0, readNum);
            }
            out.add(outByteBuf);
        } finally {
            bzip2InputStream.close();
            bais.close();
        }
    }
}
