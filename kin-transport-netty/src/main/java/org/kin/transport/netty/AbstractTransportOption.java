package org.kin.transport.netty;

import io.netty.channel.ChannelOption;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * transport配置
 *
 * @author huangjianqin
 * @date 2019/7/29
 */
public abstract class AbstractTransportOption<IN, MSG, OUT, O extends AbstractTransportOption<IN, MSG, OUT, O>> {
    /** server/selector channel 配置 */
    protected Map<ChannelOption, Object> serverOptions = new HashMap<>();
    /** channel 配置 */
    protected Map<ChannelOption, Object> channelOptions = new HashMap<>();

    protected ProtocolHandler<MSG> protocolHandler;
    protected TransportProtocolTransfer<IN, MSG, OUT> transportProtocolTransfer;

    /** 是否压缩 */
    protected CompressionType compressionType = CompressionType.NONE;
    /** 读空闲时间(秒) */
    protected int readIdleTime;
    /** 写空闲时间(秒) */
    protected int writeIdleTime;
    /** 读写空闲时间(秒) */
    protected int readWriteIdleTime;
    /** ssl */
    protected boolean ssl;
    /** 证书路径 */
    protected String certFilePath;
    /** 证书密钥路径 */
    protected String certKeyFilePath;
    /** 读超时(秒) */
    protected int readTimeout;
    /** 写超时(秒) */
    protected int writeTimeout;
    /** 连接超时(毫秒) */
    protected long connectTimeout;

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

    public CompressionType getCompressionType() {
        return compressionType;
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

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    void setProtocolHandler(ProtocolHandler<MSG> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static class TransportOptionBuilder<IN, MSG, OUT, O extends AbstractTransportOption<IN, MSG, OUT, O>> {
        /** target */
        protected final O transportOption;

        private volatile boolean exported;

        public TransportOptionBuilder(O transportOption) {
            this.transportOption = transportOption;
        }

        public O build() {
            exported = true;
            return transportOption;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> serverOptions(Map<ChannelOption, Object> channelOptions) {
            transportOption.serverOptions.putAll(channelOptions);
            return this;
        }

        public <E> TransportOptionBuilder<IN, MSG, OUT, O> serverOption(ChannelOption<E> channelOption, E value) {
            transportOption.serverOptions.put(channelOption, value);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> channelOptions(Map<ChannelOption, Object> channelOptions) {
            transportOption.channelOptions.putAll(channelOptions);
            return this;
        }

        public <E> TransportOptionBuilder<IN, MSG, OUT, O> channelOption(ChannelOption<E> channelOption, E value) {
            transportOption.channelOptions.put(channelOption, value);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> protocolHandler(ProtocolHandler<MSG> protocolHandler) {
            transportOption.protocolHandler = protocolHandler;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> transportProtocolTransfer(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
            transportOption.transportProtocolTransfer = transfer;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> compress(CompressionType compressionType) {
            transportOption.compressionType = compressionType;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> uncompress() {
            transportOption.compressionType = CompressionType.NONE;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> readIdleTime(long readIdleTime, TimeUnit unit) {
            transportOption.readIdleTime = (int) unit.toSeconds(readIdleTime);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> writeIdleTime(long writeIdleTime, TimeUnit unit) {
            transportOption.writeIdleTime = (int) unit.toSeconds(writeIdleTime);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> readWriteIdleTime(long readWriteIdleTime, TimeUnit unit) {
            transportOption.readWriteIdleTime = (int) unit.toSeconds(readWriteIdleTime);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> ssl() {
            transportOption.ssl = true;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> certFile(String certFilePath) {
            transportOption.certFilePath = certFilePath;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> certKeyFile(String certKeyFilePath) {
            transportOption.certKeyFilePath = certKeyFilePath;
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> readTimeout(long readTimeout, TimeUnit unit) {
            transportOption.readTimeout = (int) unit.toSeconds(readTimeout);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> writeTimeout(long writeTimeout, TimeUnit unit) {
            transportOption.writeTimeout = (int) unit.toSeconds(writeTimeout);
            return this;
        }

        public TransportOptionBuilder<IN, MSG, OUT, O> connectTimeout(long connectTimeout, TimeUnit unit) {
            transportOption.connectTimeout = unit.toMillis(connectTimeout);
            return this;
        }
    }
}
