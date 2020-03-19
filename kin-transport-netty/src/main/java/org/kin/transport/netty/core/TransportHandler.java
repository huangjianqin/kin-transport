package org.kin.transport.netty.core;

import io.netty.channel.Channel;
import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class TransportHandler<T extends AbstractProtocol> {
    public static TransportHandler DO_NOTHING = new TransportHandler() {
        @Override
        public void handleProtocol(Channel channel, AbstractProtocol protocol) {

        }
    };

    /**
     * 在channel线程调用
     *
     * @param protocol 协议
     */
    public abstract void handleProtocol(Channel channel, T protocol);

    /**
     * 在channel线程调用
     *
     * @param channel 触发listener的channel
     */
    public void channelActive(Channel channel) {
    }

    /**
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void channelInactive(Channel channel) {
    }

    /**
     * 在channel线程调用
     *
     * @param channel 发生异常的channel
     * @param cause   具体异常
     */
    public void handleException(Channel channel, Throwable cause) {
    }

    /**
     * 在channel线程调用
     * 流控, 拒绝服务
     *
     * @param channel  拒绝的channel
     * @param protocol 协议
     */
    public void rateLimitReject(Channel channel, AbstractProtocol protocol) {
    }

    /**
     * 在channel线程调用
     * 全局流控, 拒绝服务
     */
    public void globalRateLimitReject() {
    }

    /**
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void readWriteIdle(Channel channel) {
    }

    /**
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void readIdle(Channel channel) {
    }

    /**
     * 在channel线程调用
     *
     * @param channel 触发该listener的channel
     */
    public void writeIdel(Channel channel) {
    }
}
