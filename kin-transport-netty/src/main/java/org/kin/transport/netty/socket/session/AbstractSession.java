package org.kin.transport.netty.socket.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.kin.framework.utils.NetUtils;
import org.kin.transport.netty.socket.protocol.SocketProtocol;
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
    private AtomicBoolean flushScheduleFlag = new AtomicBoolean(false);

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
    public void write(SocketProtocol protocol) {
        write(protocol, 0, null);
    }

    /**
     * write out
     *
     * @param time     调度flush的时间
     * @param timeUnit 调度flush的时间单位
     */
    public final void write(SocketProtocol protocol, int time, TimeUnit timeUnit) {
        if (isActive() && Objects.nonNull(protocol)) {
            if (time > 0) {
                channel.writeAndFlush(protocol);
            } else {
                channel.write(protocol);
                if (flushScheduleFlag.compareAndSet(false, true)) {
                    scheduleFlush(time, timeUnit);
                }
            }
        }
    }

    /**
     * 调度flush
     */
    private void scheduleFlush(int time, TimeUnit timeUnit) {
        channel.eventLoop().schedule(() -> {
            if (flushScheduleFlag.compareAndSet(true, false)) {
                channel.flush();
            }
        }, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * write out and close session
     */
    public final void writeAndClose(SocketProtocol protocol, SessionCloseCause cause, String ip) {
        if (protocol != null) {
            ChannelFuture writeFuture = channel.writeAndFlush(protocol);
            writeFuture.addListener((ChannelFuture channelFuture) -> close(cause, ip));
            channel.eventLoop().schedule(() -> {
                if (!writeFuture.isDone()) {
                    //300ms没能write out, 则主动close
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
    public final boolean isActive() {
        return channel != null && channel.isActive();
    }

    //getter
    public final Channel getChannel() {
        return channel;
    }

    public final String getIp() {
        return ip;
    }

    public final long getIpHashCode() {
        return ipHashCode;
    }

    public final boolean isClosed() {
        return isClosed;
    }

    public final SessionCloseCause getSessionCloseCause() {
        return sessionCloseCause;
    }

    @Override
    public String toString() {
        return "Session{" +
                "channel=" + channel +
                '}';
    }
}
