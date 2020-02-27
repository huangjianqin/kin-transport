package org.kin.transport.netty.core.listener;

import io.netty.channel.Channel;

/**
 * @author huangjianqin
 * @date 2019/6/27
 */

public interface ChannelIdleListener {
    /**
     * 在channel线程调用
     * @param channel 触发该listener的channel
     */
    void allIdle(Channel channel);

    /**
     * 获取allIdle timeout时间, 秒数
     * @return allIdle timeout时间, 秒数
     */
    int allIdleTime();

    /**
     * 在channel线程调用
     * @param channel 触发该listener的channel
     */
    void readIdle(Channel channel);

    /**
     * 获取readIdle timeout时间, 秒数
     * @return readIdle timeout时间, 秒数
     */
    int readIdleTime();

    /**
     * 在channel线程调用
     * @param channel 触发该listener的channel
     */
    void writeIdel(Channel channel);

    /**
     * 获取writeIdel timeout时间, 秒数
     * @return writeIdel timeout时间, 秒数
     */
    int writeIdelTime();
}
