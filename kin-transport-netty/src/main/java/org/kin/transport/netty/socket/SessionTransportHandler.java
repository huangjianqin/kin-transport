package org.kin.transport.netty.socket;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.session.AbstractSession;
import org.kin.transport.netty.socket.session.SessionBuilder;
import org.kin.transport.netty.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 会自动创建Session的{@link TransportHandler}
 *
 * @author huangjianqin
 * @date 2020-03-19
 */
public abstract class SessionTransportHandler<T extends AbstractProtocol, S extends AbstractSession> extends TransportHandler<T> {
    private static Logger log = LoggerFactory.getLogger(SessionTransportHandler.class);
    private final AttributeKey<S> SESSION_KEY = AttributeKey.valueOf("session");
    /** seesion构建逻辑 */
    private SessionBuilder<S> sessionBuilder;

    public SessionTransportHandler(SessionBuilder<S> sessionBuilder) {
        this.sessionBuilder = sessionBuilder;
    }

    protected S session(Channel channel) {
        Attribute<S> attr = channel.attr(SESSION_KEY);
        return attr.get();
    }

    @Override
    public void channelActive(Channel channel) {
        Attribute<S> attr = channel.attr(SESSION_KEY);
        if (!attr.compareAndSet(null, sessionBuilder.create(channel))) {
            channel.close();
            log.error("Duplicate Session! IP: {}", ChannelUtils.getRemoteIp(channel));
        }
    }

    @Override
    public void channelInactive(Channel channel) {
        Attribute<S> attr = channel.attr(SESSION_KEY);
        if (Objects.nonNull(attr)) {
            attr.remove();
        }
    }
}
