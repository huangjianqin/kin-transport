package org.kin.transport.netty.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.socket.session.AbstractSession;
import org.kin.transport.netty.socket.session.SessionBuilder;
import org.kin.transport.netty.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 会自动创建Session的{@link ProtocolHandler}
 *
 * @author huangjianqin
 * @date 2020-03-19
 */
public abstract class SessionBaseSocketProtocolHandler<S extends AbstractSession> extends CachedSocketProtocolHandler {
    private static final Logger log = LoggerFactory.getLogger(SessionBaseSocketProtocolHandler.class);
    /** channel key */
    private final AttributeKey<S> SESSION_KEY = AttributeKey.valueOf("session$".concat(getClass().getSimpleName()));
    /** seesion builder */
    private final SessionBuilder<S> sessionBuilder;

    public SessionBaseSocketProtocolHandler(SessionBuilder<S> sessionBuilder) {
        this(0, null, sessionBuilder);
    }

    public SessionBaseSocketProtocolHandler(int cacheTtl, TimeUnit ttlTimeUnit, SessionBuilder<S> sessionBuilder) {
        super(cacheTtl, ttlTimeUnit);
        this.sessionBuilder = sessionBuilder;
    }

    /**
     * 根据channel获取session
     */
    protected S session(Channel channel) {
        Attribute<S> attr = channel.attr(SESSION_KEY);
        return attr.get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        Attribute<S> attr = channel.attr(SESSION_KEY);
        if (!attr.compareAndSet(null, sessionBuilder.create(channel))) {
            channel.close();
            log.error("Duplicate Session! IP: {}", ChannelUtils.getRemoteIp(channel));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        Attribute<S> attr = channel.attr(SESSION_KEY);
        if (Objects.nonNull(attr)) {
            attr.set(null);
        }
    }
}
