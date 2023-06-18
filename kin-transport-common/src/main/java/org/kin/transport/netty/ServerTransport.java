package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2023/4/1
 */
public abstract class ServerTransport<ST extends ServerTransport<ST>> extends Transport<ST> {
    private static final Logger log = LoggerFactory.getLogger(ServerTransport.class);

    /** 自定义netty child options */
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
    public <A> ST childOption(ChannelOption<A> option, A value) {
        childOptions.put(option, value);
        return (ST) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ST childOption(Map<ChannelOption, Object> childOptions) {
        this.childOptions.putAll(childOptions);
        return (ST) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getChildOptions() {
        return childOptions;
    }
}
