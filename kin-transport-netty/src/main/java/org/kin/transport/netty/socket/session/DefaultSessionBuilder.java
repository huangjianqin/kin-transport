package org.kin.transport.netty.socket.session;

import io.netty.channel.Channel;

/**
 * 默认session实现的builder
 *
 * @author huangjianqin
 * @date 2019/7/29
 */
public class DefaultSessionBuilder implements SessionBuilder<DefaultSession> {
    private static final SessionBuilder INSTANCE = new DefaultSessionBuilder();

    private DefaultSessionBuilder() {
    }

    public static SessionBuilder instance() {
        return INSTANCE;
    }

    @Override
    public DefaultSession create(Channel channel) {
        return new DefaultSession(channel);
    }
}
