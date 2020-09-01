package org.kin.transport.netty.userevent;

/**
 * 全局限流事件
 *
 * @author huangjianqin
 * @date 2020-03-19
 */
public class GlobalRatelimitEvent {
    private final long time;

    public GlobalRatelimitEvent() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }
}
