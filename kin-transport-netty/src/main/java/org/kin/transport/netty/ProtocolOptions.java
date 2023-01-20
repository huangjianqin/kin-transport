package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * 协议配置
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public class ProtocolOptions {
    /** 魔数bytes */
    private final byte[] magicBytes;
    /** 协议内容最大长度 */
    private final int maxBodySize;
    /** 协议头部长度 */
    private final int headerSize;
    /**
     * Cumulate {@link ByteBuf}s by add them to a CompositeByteBuf and so do no memory copy whenever possible.
     * Be aware that CompositeByteBuf use a more complex indexing implementation so depending on your use-case
     * and the decoder implementation this may be slower then just use the {@link io.netty.handler.codec.ByteToMessageDecoder#MERGE_CUMULATOR}.
     */
    private final boolean useCompositeBuf;

    public ProtocolOptions(String magic, int maxBodySize, boolean useCompositeBuf) {
        this.magicBytes = magic.getBytes(StandardCharsets.UTF_8);
        this.maxBodySize = maxBodySize;
        this.headerSize = magicBytes.length + 4;
        this.useCompositeBuf = useCompositeBuf;
    }

    /**
     * 获取magic bytes大小
     */
    public int getMagicSize() {
        return magicBytes.length;
    }

    //getter
    public byte[] getMagicBytes() {
        return magicBytes;
    }

    public int getMaxBodySize() {
        return maxBodySize;
    }

    public int getMaxProtocolSize() {
        return maxBodySize + headerSize;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public boolean isUseCompositeBuf() {
        return useCompositeBuf;
    }
}
