package org.kin.kinrpc.transport.netty;

import io.netty.channel.Channel;

/**
 * @author huangjianqin
 * @date 2019/7/29
 */
public class DefaultSessionBuilder implements SessionBuilder{
    private static final SessionBuilder INSTANCE = new DefaultSessionBuilder();

    private DefaultSessionBuilder() {
    }

    public static SessionBuilder instance(){
        return INSTANCE;
    }

    @Override
    public AbstractSession create(Channel channel) {
        return new AbstractSession(channel, false) {
        };
    }
}
