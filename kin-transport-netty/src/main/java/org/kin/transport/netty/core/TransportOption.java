package org.kin.transport.netty.core;

import io.netty.channel.ChannelOption;
import org.kin.transport.netty.core.protocol.DefaultProtocolTransfer;
import org.kin.transport.netty.core.protocol.ProtocolTransfer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * ransport配置
 *
 * @author huangjianqin
 * @date 2019/7/29
 */
public class TransportOption {
    private TransportHandler transportHandler = TransportHandler.DO_NOTHING;
    /** server/selector channel 配置 */
    private Map<ChannelOption, Object> serverOptions = new HashMap<>();
    /** channel 配置 */
    private Map<ChannelOption, Object> channelOptions = new HashMap<>();
    private ProtocolTransfer protocolTransfer = DefaultProtocolTransfer.instance();


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


    /** server配置 */
    public static ServerTransportOption server() {
        return new ServerTransportOption();
    }

    /** client配置 */
    public static ClientTransportOption client() {
        return new ClientTransportOption();
    }

    //------------------------------------------------------------------------------------------------------------------

    public <T extends TransportOption> T transportHandler(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
        return (T) this;
    }

    public <T extends TransportOption> T serverOptions(Map<ChannelOption, Object> channelOptions) {
        this.serverOptions.putAll(channelOptions);
        return (T) this;
    }

    public <T extends TransportOption, E> T serverOption(ChannelOption<E> channelOption, E value) {
        this.serverOptions.put(channelOption, value);
        return (T) this;
    }

    public <T extends TransportOption> T channelOptions(Map<ChannelOption, Object> channelOptions) {
        this.channelOptions.putAll(channelOptions);
        return (T) this;
    }

    public <T extends TransportOption, E> T channelOption(ChannelOption<E> channelOption, E value) {
        this.channelOptions.put(channelOption, value);
        return (T) this;
    }

    public <T extends TransportOption> T protocolTransfer(ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return (T) this;
    }

    public <T extends TransportOption> T compress() {
        this.compression = true;
        return (T) this;
    }

    public <T extends TransportOption> T uncompress() {
        this.compression = false;
        return (T) this;
    }

    public <T extends TransportOption> T globalRateLimit(int globalRateLimit) {
        this.globalRateLimit = globalRateLimit;
        return (T) this;
    }

    public <T extends TransportOption> T readIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
        return (T) this;
    }

    public <T extends TransportOption> T writeIdleTime(int writeIdleTime) {
        this.writeIdleTime = writeIdleTime;
        return (T) this;
    }

    public <T extends TransportOption> T readWriteIdleTime(int readWriteIdleTime) {
        this.readWriteIdleTime = readWriteIdleTime;
        return (T) this;
    }

    public <T extends TransportOption> T certFile(String certFilePath) {
        this.certFilePath = certFilePath;
        return (T) this;
    }

    public <T extends TransportOption> T certKeyFile(String certKeyFilePath) {
        this.certKeyFilePath = certKeyFilePath;
        return (T) this;
    }

    //getter
    public TransportHandler getTransportHandler() {
        return transportHandler;
    }

    public Map<ChannelOption, Object> getServerOptions() {
        return serverOptions;
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
    }

    public ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
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
