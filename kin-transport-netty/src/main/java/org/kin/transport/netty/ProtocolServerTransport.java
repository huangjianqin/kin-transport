package org.kin.transport.netty;

import io.netty.channel.ChannelOption;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class ProtocolServerTransport<PST extends ProtocolServerTransport<PST>> extends ProtocolTransport<PST> {
    @SuppressWarnings("rawtypes")
    private ServerObserver observer = ServerObserver.DEFAULT;
    /** 定义额外的netty child options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> childOptions = new HashMap<>();

    /**
     * 应用child option
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <V extends reactor.netty.transport.ServerTransport<?, ?>> V applyChildOptions(V serverTransport) {
        for (Map.Entry<ChannelOption, Object> entry : getChildOptions().entrySet()) {
            serverTransport = (V) serverTransport.childOption(entry.getKey(), entry.getValue());
        }
        return serverTransport;
    }

    //setter && getter
    @SuppressWarnings("unchecked")
    public <A> PST childOption(ChannelOption<A> option, A value) {
        childOptions.put(option, value);
        return (PST) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PST childOption(Map<ChannelOption, Object> childOptions) {
        this.childOptions.putAll(childOptions);
        return (PST) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getChildOptions() {
        return childOptions;
    }

    @SuppressWarnings("rawtypes")
    public ServerObserver getObserver() {
        return observer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PST observer(ServerObserver observer) {
        this.observer = observer;
        return (PST) this;
    }
}
