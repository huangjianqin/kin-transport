package org.kin.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 设置了watermark后, {@link #channelWritabilityChanged(ChannelHandlerContext)}触发对read inbound消息进行开关
 *
 * @author huangjianqin
 * @date 2022/2/13
 */
public final class ChannelWritabilityChangedHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelWritabilityChangedHandler.class);

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();
        ChannelConfig config = ch.config();

        /*
            !!!! 开关auto read注意
            不要设计成我们不能处理任何数据了就立即关闭auto read, 而我们开始能处理了就立即打开auto read.
            这个地方应该留一个缓冲地带, 也就是如果现在排队的数据达到我们预设置的一个高水位线的时候我们关闭auto read, 而低于一个低水位线的时候才打开auto read.
            不这么弄的话, 有可能就会导致我们的auto read频繁打开和关闭. auto read的每次调整都会涉及系统调用, 对性能是有影响的.

            !!!!! 这样带来一个后果就是对端发送了FIN, 然后内核将这个socket的状态变成CLOSE_WAIT.
            但是因为应用层感知不到, 所以应用层一直没有调用close. 这样的socket就会长期处于CLOSE_WAIT状态.
            特别是一些使用连接池的应用, 如果将连接归还给连接池后, 一定要记着auto read一定是打开的.
            不然就会有大量的连接处于CLOSE_WAIT状态.

            高水位线: ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK
            低水位线: ChannelOption.WRITE_BUFFER_LOW_WATER_MARK
         */
        if (!ch.isWritable()) {
            // 当前channel的缓冲区(OutboundBuffer)大小超过了WRITE_BUFFER_HIGH_WATER_MARK
            if (log.isWarnEnabled()) {
                log.warn("{} is not writable, high water mask: {}, the number of flushed entries that are not written yet: {}.",
                        ch, config.getWriteBufferHighWaterMark(), ch.unsafe().outboundBuffer().size());
            }

            config.setAutoRead(false);
        } else {
            // 曾经高于高水位线的OutboundBuffer现在已经低于WRITE_BUFFER_LOW_WATER_MARK了
            if (log.isWarnEnabled()) {
                log.warn("{} is writable(rehabilitate), low water mask: {}, the number of flushed entries that are not written yet: {}.",
                        ch, config.getWriteBufferLowWaterMark(), ch.unsafe().outboundBuffer().size());
            }

            config.setAutoRead(true);
        }
    }
}
