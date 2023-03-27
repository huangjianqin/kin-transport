package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public abstract class AbsLengthFieldBaseTransport<LBT extends AbsLengthFieldBaseTransport<LBT>> extends AbstractSocketTransport<LBT> {
    /** 默认魔数 */
    private static final byte[] DEFAULT_MAGIC = "kin-transport".getBytes(StandardCharsets.UTF_8);

    /** 魔数 */
    private byte[] magic = DEFAULT_MAGIC;
    /** 协议内容最大长度, 默认16MB */
    private int maxBodySize = 16 * 1024 * 1024;
    /**
     * Cumulate {@link ByteBuf}s by add them to a CompositeByteBuf and so do no memory copy whenever possible.
     * Be aware that CompositeByteBuf use a more complex indexing implementation so depending on your use-case
     * and the decoder implementation this may be slower then just use the {@link io.netty.handler.codec.ByteToMessageDecoder#MERGE_CUMULATOR}.
     */
    private boolean decoderUseCompositeBuf;
    /** payload逻辑处理 */
    private PayloadProcessor payloadProcessor;

    protected AbsLengthFieldBaseTransport() {
    }

    public ProtocolOptions getProtocolOptions() {
        return new ProtocolOptions(magic, maxBodySize, decoderUseCompositeBuf);
    }

    /**
     * 配置检查
     * 实现类可以重写并自定义配置检查逻辑
     */
    protected void check() {
        //检查
        Preconditions.checkNotNull(payloadProcessor, "payload processor must be not null");
    }

    @SuppressWarnings("unchecked")
    public LBT magic(byte[] magic) {
        this.magic = magic;
        return (LBT) this;
    }

    @SuppressWarnings("unchecked")
    public LBT magic(String magicStr) {
        this.magic = magicStr.getBytes(StandardCharsets.UTF_8);
        return (LBT) this;
    }

    @SuppressWarnings("unchecked")
    public LBT maxBodySize(int maxBodySize) {
        this.maxBodySize = maxBodySize;
        return (LBT) this;
    }

    @SuppressWarnings("unchecked")
    public LBT decoderUseCompositeBuf(boolean decoderUseCompositeBuf) {
        this.decoderUseCompositeBuf = decoderUseCompositeBuf;
        return (LBT) this;
    }

    @SuppressWarnings("unchecked")
    public LBT payloadProcessor(PayloadProcessor payloadProcessor) {
        this.payloadProcessor = payloadProcessor;
        return (LBT) this;
    }

    //getter
    public byte[] getMagic() {
        return magic;
    }

    public int getMaxBodySize() {
        return maxBodySize;
    }

    public boolean isDecoderUseCompositeBuf() {
        return decoderUseCompositeBuf;
    }

    public PayloadProcessor getPayloadProcessor() {
        return payloadProcessor;
    }
}
