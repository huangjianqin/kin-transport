package org.kin.transport.netty.socket.session;

import io.netty.channel.Channel;

/**
 * 默认seesion实现
 *
 * @author huangjianqin
 * @date 2020/8/20
 */
public class DefaultSession extends AbstractSession {
    public DefaultSession(Channel channel) {
        super(channel, true);
    }
}
