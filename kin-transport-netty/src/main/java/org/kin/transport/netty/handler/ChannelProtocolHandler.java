package org.kin.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.socket.ProtocolRateLimiter;
import org.kin.transport.netty.userevent.GlobalRatelimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 协议层逻辑处理
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelProtocolHandler<MSG> extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelProtocolHandler.class);
    /** 协议处理逻辑 */
    private final ProtocolHandler<MSG> protocolHandler;

    public ChannelProtocolHandler(ProtocolHandler<MSG> protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //合并解包
        List<MSG> protocols = new ArrayList<>();
        if (msg instanceof List) {
            protocols.addAll((Collection<? extends MSG>) msg);
        }
        protocols.add((MSG) msg);

        for (MSG protocol : protocols) {
            log.debug("Recv {} {}", protocol, ctx.channel().remoteAddress());

            //流控
            if (ProtocolRateLimiter.valid(protocol)) {
                protocolHandler.handle(ctx, protocol);
            } else {
                protocolHandler.rateLimitReject(ctx, protocol);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel active: {}", channel.remoteAddress());
        protocolHandler.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("channel inactive: {}", channel.remoteAddress());
        protocolHandler.channelInactive(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("encounter exception:", cause);
        protocolHandler.handleException(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                protocolHandler.readIdle(ctx);
            } else if (event.state() == IdleState.WRITER_IDLE) {
                protocolHandler.writeIdle(ctx);
            } else {
                //All IDLE
                protocolHandler.readWriteIdle(ctx);
            }
        }
        if (evt instanceof GlobalRatelimitEvent) {
            protocolHandler.globalRateLimitReject();
        }
    }
}
