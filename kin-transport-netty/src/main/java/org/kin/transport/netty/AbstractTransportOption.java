package org.kin.transport.netty;

import io.netty.channel.ChannelOption;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * transport配置
 *
 * @author huangjianqin
 * @date 2019/7/29
 */
public abstract class AbstractTransportOption<IN, MSG, OUT, O extends AbstractTransportOption<IN, MSG, OUT, O>> {
    /** server/selector channel 配置 */
    private Map<ChannelOption, Object> serverOptions = new HashMap<>();
    /** channel 配置 */
    private Map<ChannelOption, Object> channelOptions = new HashMap<>();

    private ProtocolHandler<MSG> protocolHandler;
    private TransportProtocolTransfer<IN, MSG, OUT> transportProtocolTransfer;

    /** 是否压缩 */
    private boolean compression;
    /** 全局控流 */
    private int globalRateLimit;
    /** 读空闲时间(秒) */
    private int readIdleTime;
    /** 写空闲时间(秒) */
    private int writeIdleTime;
    /** 读写空闲时间(秒) */
    private int readWriteIdleTime;
    /** ssl */
    private boolean ssl;
    /** 证书路径 */
    private String certFilePath;
    /** 证书密钥路径 */
    private String certKeyFilePath;

    //------------------------------------------------------------------------------------------------------------------
    public O serverOptions(Map<ChannelOption, Object> channelOptions) {
        this.serverOptions.putAll(channelOptions);
        return (O) this;
    }

    public <E> O serverOption(ChannelOption<E> channelOption, E value) {
        this.serverOptions.put(channelOption, value);
        return (O) this;
    }

    public O channelOptions(Map<ChannelOption, Object> channelOptions) {
        this.channelOptions.putAll(channelOptions);
        return (O) this;
    }

    public <E> O channelOption(ChannelOption<E> channelOption, E value) {
        this.channelOptions.put(channelOption, value);
        return (O) this;
    }

    public O protocolHandler(ProtocolHandler<MSG> protocolHandler) {
        this.protocolHandler = protocolHandler;
        return (O) this;
    }

    public O transportProtocolTransfer(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
        this.transportProtocolTransfer = transfer;
        return (O) this;
    }

    public O compress() {
        this.compression = true;
        return (O) this;
    }

    public O uncompress() {
        this.compression = false;
        return (O) this;
    }

    public O globalRateLimit(int globalRateLimit) {
        this.globalRateLimit = globalRateLimit;
        return (O) this;
    }

    public O readIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
        return (O) this;
    }

    public O writeIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
        return (O) this;
    }

    public O readWriteIdleTime(int readWriteIdleTime) {
        this.readWriteIdleTime = readWriteIdleTime;
        return (O) this;
    }

    public O ssl() {
        this.ssl = true;
        return (O) this;
    }

    public O certFile(String certFilePath) {
        this.certFilePath = certFilePath;
        return (O) this;
    }

    public O certKeyFile(String certKeyFilePath) {
        this.certKeyFilePath = certKeyFilePath;
        return (O) this;
    }

    //getter
    public Map<ChannelOption, Object> getServerOptions() {
        return serverOptions;
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
    }

    public ProtocolHandler<MSG> getProtocolHandler() {
        return protocolHandler;
    }

    public TransportProtocolTransfer<IN, MSG, OUT> getTransportProtocolTransfer() {
        return transportProtocolTransfer;
    }

    public boolean isCompression() {
        return compression;
    }

    public int getGlobalRateLimit() {
        return globalRateLimit;
    }

    public int getReadIdleTime() {
        return readIdleTime;
    }

    public int getWriteIdleTime() {
        return writeIdleTime;
    }

    public int getReadWriteIdleTime() {
        return readWriteIdleTime;
    }

    public boolean isSsl() {
        return ssl;
    }

    public String getCertFilePath() {
        return certFilePath;
    }

    public String getCertKeyFilePath() {
        return certKeyFilePath;
    }

    public File getCertFile() {
        File certFile = new File(getCertFilePath());
        if (!certFile.exists()) {
            throw new IllegalArgumentException("cert certFile not exists");
        }
        return certFile;
    }

    public File getCertKeyFile() {
        File certKeyFile = new File(getCertKeyFilePath());
        if (!certKeyFile.exists()) {
            throw new IllegalArgumentException("cert certKeyFile not exists");
        }
        return certKeyFile;
    }
}
