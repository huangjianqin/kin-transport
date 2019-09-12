package org.kin.transport.netty.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.kin.transport.netty.core.domain.Response;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangjianqin on 2019/5/30.
 */
public abstract class AbstractSession {
    private static final Logger log = LoggerFactory.getLogger(AbstractSession.class);

    private volatile Channel channel;
    private boolean isFlush;

    protected AtomicInteger respSNCounter = new AtomicInteger(1);
    protected volatile String ip;
    protected volatile long ipHashCode;
    protected volatile boolean isClosed;
    protected SessionCloseCause sessionCloseCause;

    private AtomicBoolean flushChannelScheduleTag = new AtomicBoolean(false);

    public AbstractSession(Channel channel, boolean isFlush) {
        this.channel = channel;
        this.ip = ChannelUtils.getIP(channel);
        this.ipHashCode = ChannelUtils.ipHashCode(ip);
        this.isFlush = isFlush;
    }

    public Channel change(Channel channel) {
        if (!isClosed) {
            Channel old = this.channel;
            this.channel = channel;
            this.ip = ChannelUtils.getIP(channel);
            this.ipHashCode = ChannelUtils.ipHashCode(ip);
            return old;
        }

        return null;
    }

    public void sendProtocol(AbstractProtocol protocol) {
        if (protocol != null) {
            Response response = protocol.write();
            if (response != null) {
                write(response);
            }
        }
    }

    public void write(Response response) {
        if (isActive()) {
            if (response != null) {
                if (isFlush) {
                    channel.writeAndFlush(response);
                } else {
                    channel.write(response);
                    if (flushChannelScheduleTag.compareAndSet(false, true)) {
                        scheduleFlush();
                    }
                }
            }
        }
    }

    private void scheduleFlush() {
        channel.eventLoop().schedule(() -> {
            if (flushChannelScheduleTag.compareAndSet(true, false)) {
                channel.flush();
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    public void writeAndClose(Response response, SessionCloseCause cause, String ip) {
        if (response != null) {
            ChannelFuture writeFuture = channel.writeAndFlush(response);
            writeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    close(cause, ip);
                }
            });
            channel.eventLoop().schedule(() -> {
                if (!writeFuture.isDone()) {
                    close(cause, ip);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    public ChannelFuture close(SessionCloseCause cause, String ip) {
        this.isClosed = true;
        this.sessionCloseCause = cause;
        if (channel.isOpen()) {
            log.info("close session('{}') due to Cause: {}", ip, cause);
            return channel.close();
        } else {
            log.info("close closedSession('{}') due to Cause: {}", ip, cause);
            return null;
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    public int getRespSN() {
        return respSNCounter.getAndIncrement();
    }

    //getter
    public Channel getChannel() {
        return channel;
    }

    public String getIp() {
        return ip;
    }

    public long getIpHashCode() {
        return ipHashCode;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public SessionCloseCause getSessionCloseCause() {
        return sessionCloseCause;
    }

    @Override
    public String toString() {
        return "Session{" +
                "channel=" + channel +
                '}';
    }
}
