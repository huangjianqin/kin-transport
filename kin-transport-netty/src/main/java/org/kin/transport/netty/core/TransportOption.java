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
    protected ProtocolHandler protocolHandler;
    protected SessionBuilder sessionBuilder;

    protected Map<ChannelOption, Object> channelOptions = new HashMap<>();
    protected Bytes2ProtocolTransfer protocolTransfer;
    protected ChannelActiveListener channelActiveListener;
    protected ChannelInactiveListener channelInactiveListener;
    protected ChannelExceptionHandler channelExceptionHandler;
    protected ChannelIdleListener channelIdleListener;

    public static TransportOption create() {
        return new TransportOption();
    }

    public TransportOption() {
        sessionBuilder = DefaultSessionBuilder.instance();
        protocolTransfer = DefaultProtocolTransfer.instance();
    }

    public TransportOption protocolHandler(ProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
        return this;
    }

    public TransportOption sessionBuilder(SessionBuilder sessionBuilder) {
        this.sessionBuilder = sessionBuilder;
        return this;
    }

    public TransportOption channelOptions(Map<ChannelOption, Object> channelOptions) {
        this.channelOptions.putAll(channelOptions);
        return this;
    }

    public <E> TransportOption channelOption(ChannelOption<E> channelOption, E value) {
        this.channelOptions.put(channelOption, value);
        return this;
    }

    public TransportOption protocolTransfer(Bytes2ProtocolTransfer transfer) {
        this.protocolTransfer = transfer;
        return this;
    }

    public TransportOption channelActiveListener(ChannelActiveListener channelActiveListener) {
        this.channelActiveListener = channelActiveListener;
        return this;
    }

    public TransportOption channelInactiveListener(ChannelInactiveListener channelInactiveListener) {
        this.channelInactiveListener = channelInactiveListener;
        return this;
    }

    public TransportOption channelExceptionHandler(ChannelExceptionHandler channelExceptionHandler) {
        this.channelExceptionHandler = channelExceptionHandler;
        return this;
    }

    public TransportOption channelIdleListener(ChannelIdleListener channelIdleListener) {
        this.channelIdleListener = channelIdleListener;
        return this;
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
}
