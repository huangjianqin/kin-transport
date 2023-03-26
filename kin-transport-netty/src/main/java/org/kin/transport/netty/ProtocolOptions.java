package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

/**
 * 协议配置
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public class ProtocolOptions {
    /** 魔数bytes */
    private final byte[] magic;
    /** 数据内容最大长度 */
    private final int maxBodySize;
    /** 协议头部长度, 即协议内容长度+魔数bytes */
    private final int headerSize;
    /**
     * Cumulate {@link ByteBuf}s by add them to a CompositeByteBuf and so do no memory copy whenever possible.
     * Be aware that CompositeByteBuf use a more complex indexing implementation so depending on your use-case
     * and the decoder implementation this may be slower then just use the {@link io.netty.handler.codec.ByteToMessageDecoder#MERGE_CUMULATOR}.
     */
    private final boolean useCompositeBuf;

    public ProtocolOptions(byte[] magic, int maxBodySize, boolean useCompositeBuf) {
        Preconditions.checkArgument(magic.length <= Protocols.MAX_MAGIC_SIZE, "max magic bytes size must be lower than " + Protocols.MAX_MAGIC_SIZE);
        Preconditions.checkArgument(maxBodySize <= Protocols.MAX_BODY_SIZE, "max body size must be lower than " + Protocols.MAX_BODY_SIZE);
        this.magic = magic;
        this.maxBodySize = maxBodySize;
        this.headerSize = magic.length + Protocols.PROTOCOL_LENGTH_MARK_BYTES;
        this.useCompositeBuf = useCompositeBuf;
    }

    /**
     * 获取magic bytes大小
     */
    public int getMagicSize() {
        return magic.length;
    }

    //getter
    public byte[] getMagic() {
        return magic;
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
