package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public abstract class ProtocolTransport<PT extends ProtocolTransport<PT>> extends Transport<PT> {
    /** 默认魔数 */
    private static final byte[] DEFAULT_MAGIC = "kin-transport".getBytes(StandardCharsets.UTF_8);

    /** 魔数 */
    private byte[] magic = DEFAULT_MAGIC;
    /** 数据内容最大长度, 默认16MB */
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
    private final List<ChannelHandler> preHandlers = new ArrayList<>();

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
        checkRequire();
    }

    @SuppressWarnings("unchecked")
    public PT magic(byte[] magic) {
        this.magic = magic;
        return (PT) this;
    }

    @SuppressWarnings("unchecked")
    public PT magic(String magicStr) {
        this.magic = magicStr.getBytes(StandardCharsets.UTF_8);
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
    public PT addHandler(ChannelHandler handler) {
        preHandlers.add(handler);
        return (PT) this;
    }

    public PT addHandlers(ChannelHandler... handler) {
        return addHandlers(Arrays.asList(handler));
    }

    @SuppressWarnings("unchecked")
    public PT addHandlers(Collection<ChannelHandler> handlers) {
        preHandlers.addAll(handlers);
        return (PT) this;
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

    public List<ChannelHandler> getPreHandlers() {
        return preHandlers;
    }

}
