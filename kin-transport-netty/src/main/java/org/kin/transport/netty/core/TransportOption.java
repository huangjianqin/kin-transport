package org.kin.transport.netty.core;

import io.netty.channel.ChannelOption;
import org.kin.transport.netty.core.listener.ChannelActiveListener;
import org.kin.transport.netty.core.listener.ChannelIdleListener;
import org.kin.transport.netty.core.listener.ChannelInactiveListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2019/7/29
 */
public class TransportOption {
    private ProtocolHandler protocolHandler;
    private SessionBuilder sessionBuilder;

    private Map<ChannelOption, Object> channelOptions = new HashMap<>();
    private Bytes2ProtocolTransfer protocolTransfer;
    private ChannelActiveListener channelActiveListener;
    private ChannelInactiveListener channelInactiveListener;
    private ChannelExceptionHandler channelExceptionHandler;
    private ChannelIdleListener channelIdleListener;

    private boolean compression;

    public TransportOption() {
        sessionBuilder = DefaultSessionBuilder.instance();
        protocolTransfer = DefaultProtocolTransfer.instance();
    }

    public static ServerTransportOption server() {
        return new ServerTransportOption();
    }

    public static ClientTransportOption client() {
        return new ClientTransportOption();
    }

    //------------------------------------------------------------------------------------------------------------------

    public <T extends TransportOption> T protocolHandler(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
        return (T) this;
    }

    public <T extends TransportOption> T sessionBuilder(SessionBuilder sessionBuilder) {
        this.sessionBuilder = sessionBuilder;
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

    public <T extends TransportOption> T channelActiveListener(ChannelActiveListener channelActiveListener) {
        this.channelActiveListener = channelActiveListener;
        return (T) this;
    }

    public <T extends TransportOption> T channelInactiveListener(ChannelInactiveListener channelInactiveListener) {
        this.channelInactiveListener = channelInactiveListener;
        return (T) this;
    }

    public <T extends TransportOption> T channelExceptionHandler(ChannelExceptionHandler channelExceptionHandler) {
        this.channelExceptionHandler = channelExceptionHandler;
        return (T) this;
    }

    public <T extends TransportOption> T channelIdleListener(ChannelIdleListener channelIdleListener) {
        this.channelIdleListener = channelIdleListener;
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

    //getter

    public ProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public SessionBuilder getSessionBuilder() {
        return sessionBuilder;
    }

    public Map<ChannelOption, Object> getChannelOptions() {
        return channelOptions;
    }

    public Bytes2ProtocolTransfer getProtocolTransfer() {
        return protocolTransfer;
    }

    public ChannelActiveListener getChannelActiveListener() {
        return channelActiveListener;
    }

    public ChannelInactiveListener getChannelInactiveListener() {
        return channelInactiveListener;
    }

    public ChannelExceptionHandler getChannelExceptionHandler() {
        return channelExceptionHandler;
    }

    public ChannelIdleListener getChannelIdleListener() {
        return channelIdleListener;
    }

    public boolean isCompression() {
        return compression;
    }
}
