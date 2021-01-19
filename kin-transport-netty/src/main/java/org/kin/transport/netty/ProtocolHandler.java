package org.kin.transport.netty;

import io.netty.channel.ChannelHandlerContext;

/**
 * 协议层逻辑实现抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class ProtocolHandler<MSG> {
    public static final ProtocolHandler DO_NOTHING = new ProtocolHandler() {
        @Override
        public void handle(ChannelHandlerContext ctx, Object protocol) {
            //do nothing
        }
    };

    /**
     * 处理解析到的协议
     * 在channel线程调用
     *
     * @param ctx      channel上下文
     * @param protocol 协议
     */
    public abstract void handle(ChannelHandlerContext ctx, MSG protocol);

    /**
     * channel有效
     * 在channel线程调用
     *
     * @param ctx channel上下文
     */
    public void channelActive(ChannelHandlerContext ctx) {
    }

    /**
     * channel无效
     * 在channel线程调用
     *
     * @param ctx channel上下文
     */
    public void channelInactive(ChannelHandlerContext ctx) {
    }

    /**
     * 异常处理
     * 在channel线程调用
     *
     * @param ctx   channel上下文
     * @param cause 具体异常
     */
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
    }

    /**
     * channel流量达到上限时触发
     * 在channel线程调用
     *
     * @param ctx      channel上下文
     * @param protocol 协议
     */
    public void rateLimitReject(ChannelHandlerContext ctx, MSG protocol) {
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
     * @param ctx channel上下文
     */
    public void readWriteIdle(ChannelHandlerContext ctx) {
    }

    /**
     * 读空闲
     * 在channel线程调用
     *
     * @param ctx channel上下文
     */
    public void readIdle(ChannelHandlerContext ctx) {
    }

    /**
     * 写空闲
     * 在channel线程调用
     *
     * @param ctx channel上下文
     */
    public void writeIdle(ChannelHandlerContext ctx) {
    }
}
