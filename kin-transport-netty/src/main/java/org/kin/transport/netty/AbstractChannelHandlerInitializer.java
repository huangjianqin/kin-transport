package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelHandler;
import org.kin.transport.netty.handler.ChannelProtocolHandler;
import org.kin.transport.netty.handler.TransportProtocolCodec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 增加前后handlers定义
 *
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractChannelHandlerInitializer<IN, MSG, OUT, TO extends AbstractTransportOption<IN, MSG, OUT>>
        implements ChannelHandlerInitializer<IN, MSG, OUT> {
    protected final TO transportOption;

    protected AbstractChannelHandlerInitializer(TO transportOption) {
        this.transportOption = transportOption;
    }

    /**
     * 前面的handlers
     */
    protected Collection<ChannelHandler> firstHandlers() {
        return Collections.emptyList();
    }

    /**
     * 后面的handlers
     */
    protected Collection<ChannelHandler> lastHandlers() {
        return Collections.emptyList();
    }

    @Override
    public ChannelHandler[] getChannelHandlers() {
        TransportProtocolTransfer<IN, MSG, OUT> transportProtocolTransfer = transportOption.getTransportProtocolTransfer();
        ProtocolHandler<MSG> protocolHandler = transportOption.getProtocolHandler();

        Preconditions.checkNotNull(transportProtocolTransfer, "transportProtocolTransfer must not null");
        Preconditions.checkNotNull(protocolHandler, "protocolHandler must not null");

        List<ChannelHandler> channelHandlers = new ArrayList<>(firstHandlers());
        channelHandlers.add(new TransportProtocolCodec<>(transportProtocolTransfer));
        channelHandlers.add(new ChannelProtocolHandler<>(protocolHandler));
        channelHandlers.addAll(lastHandlers());
        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}
