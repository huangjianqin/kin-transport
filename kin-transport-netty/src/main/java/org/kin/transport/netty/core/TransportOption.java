package org.kin.transport.netty.core;

import io.netty.channel.ChannelOption;
import org.kin.transport.netty.core.protocol.Bytes2ProtocolTransfer;
import org.kin.transport.netty.core.protocol.DefaultProtocolTransfer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2019/7/29
 */
public class TransportOption {
    private TransportHandler transportHandler = TransportHandler.DO_NOTHING;
    private Map<ChannelOption, Object> channelOptions = new HashMap<>();
    private Bytes2ProtocolTransfer protocolTransfer = DefaultProtocolTransfer.instance();
    ;

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

    public static ServerTransportOption server() {
        return new ServerTransportOption();
    }

    public static ClientTransportOption client() {
        return new ClientTransportOption();
    }

    //------------------------------------------------------------------------------------------------------------------

    public <T extends TransportOption> T transportHandler(TransportHandler transportHandler) {
        this.transportHandler = transportHandler;
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

    public <T extends TransportOption> T protocolTransfer(Bytes2ProtocolTransfer transfer) {
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

    //getter
    public TransportHandler getTransportHandler() {
        return transportHandler;
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
    }

    public Bytes2ProtocolTransfer getProtocolTransfer() {
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
}
