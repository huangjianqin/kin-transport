package org.kin.transport.netty;

import io.netty.channel.ChannelOption;

import java.util.HashMap;
import java.util.Map;

/**
 * 传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport<T extends Transport<T>> {
    protected static final String[] PROTOCOLS = new String[]{"TLSv1.3", "TLSv.1.2"};

    /** ssl */
    private boolean ssl;
    /** 定义额外的netty options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> options = new HashMap<>();

    /**
     * 检查配置合法性
     */
    protected void checkRequire() {
        //default do nothing
    }

    /**
     * 应用option
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <V extends reactor.netty.transport.Transport<?, ?>> V applyOptions(V transport) {
        for (Map.Entry<ChannelOption, Object> entry : getOptions().entrySet()) {
            transport = (V) transport.option(entry.getKey(), entry.getValue());
        }
        return transport;
    }

    //setter && getter
    public boolean isSsl() {
        return ssl;
    }

    @SuppressWarnings("unchecked")
    public T ssl(boolean ssl) {
        this.ssl = ssl;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <A> T option(ChannelOption<A> option, A value) {
        options.put(option, value);
        return (T) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public T option(Map<ChannelOption, Object> options) {
        this.options.putAll(options);
        return (T) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getOptions() {
        return options;
    }
}
