package org.kin.transport.netty.core.common;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.core.session.AbstractSession;

/**
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelAttrKeys {
    private ChannelAttrKeys() {

    }

    public static final AttributeKey<AbstractSession> SESSION_KEY = AttributeKey.valueOf("session");

    public static AbstractSession session(Channel channel) {
        Attribute<AbstractSession> attr = channel.attr(ChannelAttrKeys.SESSION_KEY);
        return attr.get();
    }
}
