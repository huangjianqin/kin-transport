package org.kin.transport.netty.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.kin.framework.concurrent.SimpleThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * 基于 {@link HashedWheelTimer} 的空闲链路监测.
 * 1. Netty4.x默认的链路检测使用的是eventLoop的delayQueue, delayQueue是一个优先级队列, 复杂度为O(log n),
 * 每个worker处理自己的链路监测, 可能有助于减少上下文切换, 但是网络IO操作与idle会相互影响.
 * 2. 这个实现使用{@link HashedWheelTimer}的复杂度为O(1), 而且网络IO操作与idle不会相互影响, 但是有上下文切换.
 * 3. 如果连接数小, 比如几万以内, 可以直接用Netty4.x默认的链路检测 {@link io.netty.handler.timeout.IdleStateHandler},
 * 如果连接数较大, 建议使用这个实现.
 *
 * @author huangjianqin
 * @date 2021/12/8
 */
public class IdleStateHandler extends ChannelDuplexHandler {
    /** 最小空闲时间, 默认1ms */
    private static final long MIN_TIMEOUT_MILLIS = 1;
    /** 状态-初始 */
    private static final byte NONE = 0;
    /** 状态-初始化后 */
    private static final byte INITIALIZED = 1;
    /** 状态-destroyed */
    private static final byte DESTROYED = 2;

    /** 默认写操作完成listener */
    private final ChannelFutureListener writeListener = new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) {
            // make hb for firstWriterIdleEvent and firstAllIdleEvent
            firstWriterIdleEvent = firstAllIdleEvent = true;
            lastWriteTime = System.currentTimeMillis();
        }
    };

    private final HashedWheelTimer timer;
    /** 读空闲时间ms */
    private final long readerIdleTimeMillis;
    /** 写空闲时间ms */
    private final long writerIdleTimeMillis;
    /** 读写空闲时间ms */
    private final long allIdleTimeMillis;

    /** 0 - none, 1 - initialized, 2 - destroyed */
    private volatile int state;
    /** 是否正在读 */
    private volatile boolean reading;

    /** 读空闲超时task */
    private volatile Timeout readerIdleTimeout;
    /** 上次读完成时间 */
    private volatile long lastReadTime;
    /** 是否是第一次读空闲事件(即发生过读操作后第一次空闲, 区分连续读空闲) */
    private boolean firstReaderIdleEvent = true;

    /** 写空闲超时task */
    private volatile Timeout writerIdleTimeout;
    /** 上次写完成时间 */
    private volatile long lastWriteTime;
    /** 是否是第一次写空闲事件(即发生过写操作后第一次空闲, 区分连续写空闲) */
    private boolean firstWriterIdleEvent = true;

    /** 读写空闲超时task */
    private volatile Timeout allIdleTimeout;
    /** 是否是第一次读写空闲事件(即发生过读写操作后第一次空闲, 区分连续读写空闲) */
    private boolean firstAllIdleEvent = true;

    public IdleStateHandler(
            int readerIdleTimeSeconds,
            int writerIdleTimeSeconds,
            int allIdleTimeSeconds) {

        this(new HashedWheelTimer(new SimpleThreadFactory("netty-idleStateHandler", true)), readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public IdleStateHandler(
            HashedWheelTimer timer,
            int readerIdleTimeSeconds,
            int writerIdleTimeSeconds,
            int allIdleTimeSeconds) {

        this(timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, TimeUnit.SECONDS);
    }

    public IdleStateHandler(
            HashedWheelTimer timer,
            long readerIdleTime,
            long writerIdleTime,
            long allIdleTime,
            TimeUnit unit) {

        if (unit == null) {
            throw new NullPointerException("unit");
        }

        this.timer = timer;

        if (readerIdleTime <= 0) {
            readerIdleTimeMillis = 0;
        } else {
            readerIdleTimeMillis = Math.max(unit.toMillis(readerIdleTime), MIN_TIMEOUT_MILLIS);
        }
        if (writerIdleTime <= 0) {
            writerIdleTimeMillis = 0;
        } else {
            writerIdleTimeMillis = Math.max(unit.toMillis(writerIdleTime), MIN_TIMEOUT_MILLIS);
        }
        if (allIdleTime <= 0) {
            allIdleTimeMillis = 0;
        } else {
            allIdleTimeMillis = Math.max(unit.toMillis(allIdleTime), MIN_TIMEOUT_MILLIS);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel ch = ctx.channel();

        if (ch.isActive() && ch.isRegistered()) {
            // channelActive() event has been fired already, which means this.channelActive() will
            // not be invoked. We have to initialize here instead.
            initialize(ctx);
        } else {
            // channelActive() event has not been fired yet.  this.channelActive() will be invoked
            // and initialization will occur there.
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        destroy();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        // Initialize early if channel is active already.
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // This method will be invoked only if this handler was added
        // before channelActive() event is fired.  If a user adds this handler
        // after the channelActive() event, initialize() will be called by beforeAdd().
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (readerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            // make hb for firstReaderIdleEvent and firstAllIdleEvent
            firstReaderIdleEvent = firstAllIdleEvent = true;
            reading = true;
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (readerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            // make hb for firstReaderIdleEvent and firstAllIdleEvent
            lastReadTime = System.currentTimeMillis();
            reading = false;
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (writerIdleTimeMillis > 0 || allIdleTimeMillis > 0) {
            if (promise.isVoid()) {
                // make hb for firstWriterIdleEvent and firstAllIdleEvent
                firstWriterIdleEvent = firstAllIdleEvent = true;
                lastWriteTime = System.currentTimeMillis();
            } else {
                promise.addListener(writeListener);
            }
        }
        ctx.write(msg, promise);
    }

    private void initialize(ChannelHandlerContext ctx) {
        // Avoid the case where destroy() is called before scheduling timeouts.
        // See: https://github.com/netty/netty/issues/143
        switch (state) {
            case INITIALIZED:
            case DESTROYED:
                return;
        }

        state = INITIALIZED;

        lastReadTime = lastWriteTime = System.currentTimeMillis();
        if (readerIdleTimeMillis > 0) {
            readerIdleTimeout = timer.newTimeout(
                    new ReaderIdleTimeoutTask(ctx),
                    readerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (writerIdleTimeMillis > 0) {
            writerIdleTimeout = timer.newTimeout(
                    new WriterIdleTimeoutTask(ctx),
                    writerIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
        if (allIdleTimeMillis > 0) {
            allIdleTimeout = timer.newTimeout(
                    new AllIdleTimeoutTask(ctx),
                    allIdleTimeMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void destroy() {
        state = DESTROYED;

        if (readerIdleTimeout != null) {
            readerIdleTimeout.cancel();
            readerIdleTimeout = null;
        }
        if (writerIdleTimeout != null) {
            writerIdleTimeout.cancel();
            writerIdleTimeout = null;
        }
        if (allIdleTimeout != null) {
            allIdleTimeout.cancel();
            allIdleTimeout = null;
        }
    }

    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        ctx.fireUserEventTriggered(evt);
    }

    //getter
    public long getReaderIdleTimeMillis() {
        return readerIdleTimeMillis;
    }

    public long getWriterIdleTimeMillis() {
        return writerIdleTimeMillis;
    }

    public long getAllIdleTimeMillis() {
        return allIdleTimeMillis;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * 读空闲超时task
     */
    private final class ReaderIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        ReaderIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long lastReadTime = IdleStateHandler.this.lastReadTime;
            long nextDelay = readerIdleTimeMillis;
            if (!reading) {
                nextDelay -= System.currentTimeMillis() - lastReadTime;
            }
            if (nextDelay <= 0) {
                // Reader is idle - set a new timeout and notify the callback.
                readerIdleTimeout = timer.newTimeout(this, readerIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstReaderIdleEvent) {
                        firstReaderIdleEvent = false;
                        event = IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.READER_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                readerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * 写空闲超时task
     */
    private final class WriterIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        WriterIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long lastWriteTime = IdleStateHandler.this.lastWriteTime;
            long nextDelay = writerIdleTimeMillis - (System.currentTimeMillis() - lastWriteTime);
            if (nextDelay <= 0) {
                // Writer is idle - set a new timeout and notify the callback.
                writerIdleTimeout = timer.newTimeout(this, writerIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstWriterIdleEvent) {
                        firstWriterIdleEvent = false;
                        event = IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.WRITER_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                writerIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * 读写空闲超时task
     */
    private final class AllIdleTimeoutTask implements TimerTask {

        private final ChannelHandlerContext ctx;

        AllIdleTimeoutTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run(Timeout timeout) {
            if (timeout.isCancelled() || !ctx.channel().isOpen()) {
                return;
            }

            long nextDelay = allIdleTimeMillis;
            if (!reading) {
                long lastIoTime = Math.max(lastReadTime, lastWriteTime);
                nextDelay -= System.currentTimeMillis() - lastIoTime;
            }
            if (nextDelay <= 0) {
                // Both reader and writer are idle - set a new timeout and
                // notify the callback.
                allIdleTimeout = timer.newTimeout(this, allIdleTimeMillis, TimeUnit.MILLISECONDS);
                try {
                    IdleStateEvent event;
                    if (firstAllIdleEvent) {
                        firstAllIdleEvent = false;
                        event = IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT;
                    } else {
                        event = IdleStateEvent.ALL_IDLE_STATE_EVENT;
                    }
                    channelIdle(ctx, event);
                } catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            } else {
                // Either read or write occurred before the timeout - set a new
                // timeout with shorter delay.
                allIdleTimeout = timer.newTimeout(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }
    }
}
