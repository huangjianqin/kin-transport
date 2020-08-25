package org.kin.transport.netty;

import io.netty.channel.Channel;

/**
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class TransportHandler<MSG> {
    public static TransportHandler DO_NOTHING = new TransportHandler() {
        @Override
        public void handle(Channel channel, Object protocol) {
            //do nothing
        }
    };

    /**
     * 处理解析到的协议
     * 在channel线程调用
     *
     * @param protocol 协议
     */
    public abstract void handle(Channel channel, MSG protocol);

    /**
     * channel有效
     * 在channel线程调用
     *
     * @param channel 触发listener的channel
     */
    public void channelActive(Channel channel) {
    }

    /**
     * channel无效
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void channelInactive(Channel channel) {
    }

    /**
     * 异常处理
     * 在channel线程调用
     *
     * @param channel 发生异常的channel
     * @param cause   具体异常
     */
    public void handleException(Channel channel, Throwable cause) {
    }

    /**
     * channel流量达到上限时触发
     * 在channel线程调用
     *
     * @param channel  channel
     * @param protocol 协议
     */
    public void rateLimitReject(Channel channel, MSG protocol) {
    }

    /**
     * 全局流量达到上限时触发
     * 在channel线程调用
     */
    public void globalRateLimitReject() {
    }

    /**
     * 读写空闲
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void readWriteIdle(Channel channel) {
    }

    /**
     * 读空闲
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void readIdle(Channel channel) {
    }

    /**
     * 写空闲
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void writeIdel(Channel channel) {
    }
}
