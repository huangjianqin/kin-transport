package org.kin.transport.netty.core.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import org.kin.transport.netty.core.*;
import org.kin.transport.netty.core.common.ProtocolConstants;
import org.kin.transport.netty.core.listener.ChannelActiveListener;
import org.kin.transport.netty.core.listener.ChannelInactiveListener;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ChannelProtocolHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ChannelProtocolHandler.class);
    private final ProtocolHandler protocolHandler;
    private final SessionBuilder sessionBuilder;
    private ChannelActiveListener channelActiveListener;
    private ChannelInactiveListener channelInactiveListener;
    private ChannelExceptionHandler channelExceptionHandler;

    public ChannelProtocolHandler(ProtocolHandler protocolHandler,
                                  SessionBuilder sessionBuilder,
                                  ChannelActiveListener channelActiveListener,
                                  ChannelInactiveListener channelInactiveListener,
                                  ChannelExceptionHandler channelExceptionHandler) {
        this.protocolHandler = protocolHandler;
        this.sessionBuilder = sessionBuilder;
        this.channelActiveListener = channelActiveListener;
        this.channelInactiveListener = channelInactiveListener;
        this.channelExceptionHandler = channelExceptionHandler;
    }

    public ChannelProtocolHandler(ProtocolHandler protocolHandler, SessionBuilder sessionBuilder) {
        this(protocolHandler, sessionBuilder, null, null, null);
    }

    public ChannelProtocolHandler(ProtocolHandler protocolHandler,
                                  SessionBuilder sessionBuilder,
                                  ChannelActiveListener channelActiveListener) {
        this(protocolHandler, sessionBuilder, channelActiveListener, null, null);
    }

    public ChannelProtocolHandler(ProtocolHandler protocolHandler,
                                  SessionBuilder sessionBuilder,
                                  ChannelInactiveListener channelInactiveListener) {
        this(protocolHandler, sessionBuilder, null, channelInactiveListener, null);
    }

    public ChannelProtocolHandler(ProtocolHandler protocolHandler,
                                  SessionBuilder sessionBuilder,
                                  ChannelExceptionHandler channelExceptionHandler) {
        this(protocolHandler, sessionBuilder, null, null, channelExceptionHandler);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //合并解包
        List<AbstractProtocol> protocols = new ArrayList<>();
        if (msg instanceof List) {
            protocols.addAll((Collection<? extends AbstractProtocol>) msg);
        }
        if (msg instanceof AbstractProtocol) {
            protocols.add((AbstractProtocol) msg);
        }

        for (AbstractProtocol protocol : protocols) {
            log.debug("Recv {} {} {}", protocol.getProtocolId(), protocol.getClass().getSimpleName(), ctx.channel());

            if(ProtocolRateLimiter.valid(protocol, protocolHandler)){
                AbstractSession session = ProtocolConstants.session(ctx.channel());
                if (session != null) {
                    protocolHandler.handleProtocol(session, protocol);
                }
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel active: {}", ctx.channel());
        Attribute<AbstractSession> attr = ctx.channel().attr(ProtocolConstants.SESSION_KEY);
        if (!attr.compareAndSet(null, sessionBuilder.create(ctx.channel()))) {
            ctx.channel().close();
            log.error("Duplicate Session! IP: {}", ChannelUtils.getIP(ctx.channel()));
            return;
        }
        if (channelActiveListener != null) {
            try {
                channelActiveListener.channelActive(ctx.channel());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channel inactive: {}", ctx.channel());
        if (channelInactiveListener != null) {
            try {
                channelInactiveListener.channelInactive(ctx.channel());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        log.error("server('{}') throw exception:{}", ChannelUtils.getIP(channel), cause);
        if (channel.isOpen() || channel.isActive()) {
            ctx.close();
        }
        if (channelExceptionHandler != null) {
            try {
                channelExceptionHandler.handleException(ctx.channel(), cause);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
