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
    protected Map<ChannelOption, Object> selectorOptions = new HashMap<>();
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
    /** 绑定端口 | 连接 超时(毫秒) */
    protected long awaitTimeout;

    //getter
    public Map<ChannelOption, Object> getSelectorOptions() {
        return selectorOptions;
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

    public long getAwaitTimeout() {
        return awaitTimeout;
    }

    void setProtocolHandler(ProtocolHandler<MSG> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    //------------------------------------------------------builder------------------------------------------------------
    public static class TransportOptionBuilder<IN, MSG, OUT, O extends AbstractTransportOption<IN, MSG, OUT, O>, B extends TransportOptionBuilder<IN, MSG, OUT, O, B>> {
        /** target */
        protected final O transportOption;

        private volatile boolean exported;

        public TransportOptionBuilder(O transportOption) {
            this.transportOption = transportOption;
        }

        /**
         * 暴露transport options
         */
        public O build() {
            checkState();
            exported = true;
            return transportOption;
        }

        /**
         * 配置是否使用过(绑定过端口或者连接过远程服务器), 则配置内容不能再修改
         */
        public boolean isExported() {
            return exported;
        }

        /**
         * 检查是否exported
         */
        protected void checkState() {
            if (exported) {
                throw new IllegalStateException("transport options is exported!!! can not change");
            }
        }

        @SuppressWarnings("unchecked")
        public B selectorOptions(Map<ChannelOption, Object> channelOptions) {
            checkState();
            transportOption.selectorOptions.putAll(channelOptions);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public <E> B selectorOption(ChannelOption<E> channelOption, E value) {
            checkState();
            transportOption.selectorOptions.put(channelOption, value);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B channelOptions(Map<ChannelOption, Object> channelOptions) {
            checkState();
            transportOption.channelOptions.putAll(channelOptions);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public <E> B channelOption(ChannelOption<E> channelOption, E value) {
            checkState();
            transportOption.channelOptions.put(channelOption, value);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B protocolHandler(ProtocolHandler<MSG> protocolHandler) {
            checkState();
            transportOption.protocolHandler = protocolHandler;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B transportProtocolTransfer(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
            checkState();
            transportOption.transportProtocolTransfer = transfer;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B compress(CompressionType compressionType) {
            checkState();
            transportOption.compressionType = compressionType;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B uncompress() {
            checkState();
            transportOption.compressionType = CompressionType.NONE;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B readIdleTime(long readIdleTime, TimeUnit unit) {
            checkState();
            transportOption.readIdleTime = (int) unit.toSeconds(readIdleTime);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B writeIdleTime(long writeIdleTime, TimeUnit unit) {
            checkState();
            transportOption.writeIdleTime = (int) unit.toSeconds(writeIdleTime);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B readWriteIdleTime(long readWriteIdleTime, TimeUnit unit) {
            checkState();
            transportOption.readWriteIdleTime = (int) unit.toSeconds(readWriteIdleTime);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B ssl(String certFilePath, String certKeyFilePath) {
            checkState();
            transportOption.ssl = true;
            transportOption.certFilePath = certFilePath;
            transportOption.certKeyFilePath = certKeyFilePath;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B readTimeout(long readTimeout, TimeUnit unit) {
            checkState();
            transportOption.readTimeout = (int) unit.toSeconds(readTimeout);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B writeTimeout(long writeTimeout, TimeUnit unit) {
            checkState();
            transportOption.writeTimeout = (int) unit.toSeconds(writeTimeout);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B awaitTimeout(long connectTimeout, TimeUnit unit) {
            checkState();
            transportOption.awaitTimeout = unit.toMillis(connectTimeout);
            return (B) this;
        }
    }
}
