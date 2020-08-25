package org.kin.transport.netty.http.server.session;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.transport.netty.socket.session.AbstractSession;

import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpSession extends AbstractSession {
    private static final AttributeKey<HttpSession> SESSION_KEY = AttributeKey.valueOf("session");

    /** http请求 */
    private final FullHttpRequest request;

    public HttpSession(Channel channel, FullHttpRequest request) {
        super(channel, true);
        this.request = request;
    }

    //---------------------------------------------------------------------------------------------------------------
    public static void put(Channel channel, FullHttpRequest request) {
        Attribute<HttpSession> attr = channel.attr(SESSION_KEY);
        attr.compareAndSet(null, new HttpSession(channel, request));
    }

    public static void remove(Channel channel) {
        Attribute<HttpSession> attr = channel.attr(SESSION_KEY);
        if (Objects.nonNull(attr)) {
            attr.remove();
        }
    }

    public static HttpSession get(Channel channel) {
        Attribute<HttpSession> attr = channel.attr(SESSION_KEY);
        if (Objects.nonNull(attr)) {
            return attr.get();
        }

        return null;
    }

    //---------------------------------------------------------------------------------------------------------------

    //getter
    public FullHttpRequest getRequest() {
        return request;
    }
}
