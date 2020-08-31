package org.kin.transport.netty.userevent;

/**
 * 全局限流事件
 *
 * @author huangjianqin
 * @date 2020-03-19
 */
public class GlobalRatelimitEvent {
    public static final GlobalRatelimitEvent INSTANCE = new GlobalRatelimitEvent();

    private GlobalRatelimitEvent() {
    }
}
