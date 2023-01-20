package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public abstract class ProtocolTransport<PT extends ProtocolTransport<PT>> extends Transport<PT> {
    /** 魔数 */
    private String magic = "kin-transport";
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
    /** 定义前置handler */
    private PreHandlerInitializer preHandlerInitializer = PreHandlerInitializer.DEFAULT;

    protected ProtocolTransport() {
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
    public PT magic(String magic) {
        this.magic = magic;
        return (PT) this;
    }

    @SuppressWarnings("unchecked")
    public PT maxBodySize(int maxBodySize) {
        this.maxBodySize = maxBodySize;
        return (PT) this;
    }

    @SuppressWarnings("unchecked")
    public PT decoderUseCompositeBuf(boolean decoderUseCompositeBuf) {
        this.decoderUseCompositeBuf = decoderUseCompositeBuf;
        return (PT) this;
    }

    @SuppressWarnings("unchecked")
    public PT payloadProcessor(PayloadProcessor payloadProcessor) {
        this.payloadProcessor = payloadProcessor;
        return (PT) this;
    }

    @SuppressWarnings("unchecked")
    public PT preHandlerCustomizer(PreHandlerInitializer preHandlerInitializer) {
        this.preHandlerInitializer = preHandlerInitializer;
        return (PT) this;
    }

    //getter
    public String getMagic() {
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

    public PreHandlerInitializer getPreHandlerCustomizer() {
        return preHandlerInitializer;
    }
}
