package org.kin.transport.netty;

import io.netty.channel.ChannelHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2023/3/27
 */
public abstract class AbstractSocketTransport<ST extends AbstractSocketTransport<ST>> extends AbstractTransport<ST> {
    /** 定义前置handler */
    private final List<ChannelHandler> preHandlers = new ArrayList<>();

    //setter && getter
    @SuppressWarnings("unchecked")
    public ST addHandler(ChannelHandler handler) {
        preHandlers.add(handler);
        return (ST) this;
    }

    public ST addHandlers(ChannelHandler... handler) {
        return addHandlers(Arrays.asList(handler));
    }

    @SuppressWarnings("unchecked")
    public ST addHandlers(Collection<ChannelHandler> handlers) {
        preHandlers.addAll(handlers);
        return (ST) this;
    }

    public List<ChannelHandler> getPreHandlers() {
        return preHandlers;
    }
}
