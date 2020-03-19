package org.kin.transport.netty.core;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import org.kin.transport.netty.core.common.ChannelAttrKeys;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.session.AbstractSession;
import org.kin.transport.netty.core.session.SessionBuilder;
import org.kin.transport.netty.core.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020-03-19
 */
public abstract class SessionTransportHandler<T extends AbstractProtocol, S extends AbstractSession> extends TransportHandler<T> {
    private static Logger log = LoggerFactory.getLogger(SessionTransportHandler.class);
    private SessionBuilder<S> sessionBuilder;

    public SessionTransportHandler(SessionBuilder<S> sessionBuilder) {
        this.sessionBuilder = sessionBuilder;
    }

    public abstract void handleProtocol(S seesion, AbstractProtocol protocol);

    public abstract void handleException(S seesion, Throwable cause);

    private S session(Channel channel) {
        Attribute<AbstractSession> attr = channel.attr(ChannelAttrKeys.SESSION_KEY);
        return (S) attr.get();
    }

    @Override
    public final void handleProtocol(Channel channel, AbstractProtocol protocol) {
        handleProtocol(session(channel), protocol);
    }

    @Override
    public void channelActive(Channel channel) {
        Attribute<AbstractSession> attr = channel.attr(ChannelAttrKeys.SESSION_KEY);
        if (!attr.compareAndSet(null, sessionBuilder.create(channel))) {
            channel.close();
            log.error("Duplicate Session! IP: {}", ChannelUtils.getIP(channel));
        }
    }

    @Override
    public void channelInactive(Channel channel) {
        Attribute<AbstractSession> attr = channel.attr(ChannelAttrKeys.SESSION_KEY);
        if (Objects.nonNull(attr)) {
            attr.remove();
        }
    }

    @Override
    public void handleException(Channel channel, Throwable cause) {
        handleException(session(channel), cause);
    }
}
