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
public abstract class TransportOption {
    /** server/selector channel 配置 */
    private Map<ChannelOption, Object> serverOptions = new HashMap<>();
    /** channel 配置 */
    private Map<ChannelOption, Object> channelOptions = new HashMap<>();

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
    public Map<ChannelOption, Object> getServerOptions() {
        return serverOptions;
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
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
