package org.kin.transport.netty.socket.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.socket.domain.SessionCloseCause;
import org.kin.transport.netty.socket.protocol.AbstractProtocol;
import org.kin.transport.netty.socket.protocol.domain.Response;
import org.kin.transport.netty.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * session抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class AbstractSession {
    private static final Logger log = LoggerFactory.getLogger(AbstractSession.class);
    /** session绑定的channel */
    private volatile Channel channel;
    /** 标识每次write, 是否马上flush */
    private boolean isFlush;

    /** session ip */
    private volatile String ip;
    /** session ip hash */
    private volatile long ipHashCode;
    /** 标识是否closed */
    private volatile boolean isClosed;
    /** session closed原因 */
    private SessionCloseCause sessionCloseCause;
    /** 标识是否正调度flush */
    private AtomicBoolean flushChannelScheduleTag = new AtomicBoolean(false);

    public AbstractSession(Channel channel, boolean isFlush) {
        this.channel = channel;
        this.ip = ChannelUtils.getRemoteIp(channel);
        this.ipHashCode = NetUtils.ipHashCode(ip);
        this.isFlush = isFlush;
    }

    /**
     * seesion 切换channel
     */
    public final Channel change(Channel channel) {
        if (!isClosed) {
            Channel old = this.channel;
            this.channel = channel;
            this.ip = ChannelUtils.getRemoteIp(channel);
            this.ipHashCode = NetUtils.ipHashCode(ip);
            return old;
        }

        return null;
    }

    /**
     * write out
     */
    public void sendProtocol(AbstractProtocol protocol) {
        if (Objects.nonNull(protocol)) {
            write(protocol.write());
        }
    }

    /**
     * write out
     */
    protected final void write(Response response) {
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

    /**
     * 调度flush
     */
    private void scheduleFlush() {
        channel.eventLoop().schedule(() -> {
            if (flushChannelScheduleTag.compareAndSet(true, false)) {
                channel.flush();
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * write out and close session
     */
    public final void writeAndClose(Response response, SessionCloseCause cause, String ip) {
        if (response != null) {
            ChannelFuture writeFuture = channel.writeAndFlush(response);
            writeFuture.addListener((ChannelFuture channelFuture) -> close(cause, ip));
            channel.eventLoop().schedule(() -> {
                if (!writeFuture.isDone()) {
                    close(cause, ip);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * close session
     */
    public final ChannelFuture close(SessionCloseCause cause, String ip) {
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

    /**
     * session是否有效
     */
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

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
