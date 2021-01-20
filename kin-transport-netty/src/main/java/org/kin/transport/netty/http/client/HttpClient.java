package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandlerContext;
import org.kin.framework.concurrent.lock.OneLock;
import org.kin.framework.utils.CollectionUtils;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;
import org.kin.transport.netty.ProtocolHandler;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 真正的http client, 用于处理底层传输
 *
 * @author huangjianqin
 * @date 2020/9/8
 */
class HttpClient extends Client<HttpEntity> {
    /**
     * 假设服务端和客户端都遵循http pipeline规定(req resp req resp一个接一个的形式)
     * <p>
     * key -> netty channel id
     * value -> http call 队列
     */
    private static final Map<String, Queue<HttpCallFuture>> CHANNEL_ID_2_CALLS = new ConcurrentHashMap<>();

    public HttpClient(AbstractTransportOption<?, ?, ?, ?> transportOption, ChannelHandlerInitializer<?, ?, ?> channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    //-------------------------------------------------------------------------------------------------------------
    Future<HttpResponse> request(HttpCall httpCall) {
        CountDownLatch latch = new CountDownLatch(1);
        HttpCallFuture httpCallFuture = new HttpCallFuture(httpCall);
        super.sendAndFlush(httpCall.getRequest(), future -> {
            if (future.isSuccess()) {
                //成功write
                String channelId = future.channel().id().asLongText();
                CHANNEL_ID_2_CALLS.get(channelId).offer(httpCallFuture);
            } else {
                httpCallFuture.done(null);
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            httpCallFuture.done(null);
        }
        return httpCallFuture;
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * http client protocol handler
     */
    static class HttpClientProtocolHandler extends ProtocolHandler<HttpEntity> {
        static final HttpClientProtocolHandler INSTANCE = new HttpClientProtocolHandler();

        private HttpClientProtocolHandler() {
        }

        @Override
        public void handle(ChannelHandlerContext ctx, HttpEntity protocol) {
            if (!(protocol instanceof HttpResponse)) {
                return;
            }
            String channelId = ctx.channel().id().asLongText();

            HttpResponse response = (HttpResponse) protocol;
            Queue<HttpCallFuture> callFutures = CHANNEL_ID_2_CALLS.get(channelId);
            if (CollectionUtils.isEmpty(callFutures)) {
                throw new IllegalArgumentException("empty call");
            }
            HttpCallFuture callFuture = callFutures.poll();
            callFuture.done(response);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            String channelId = ctx.channel().id().asLongText();
            Queue<HttpCallFuture> callFutures = CHANNEL_ID_2_CALLS.get(channelId);
            if (Objects.nonNull(callFutures)) {
                retryCalls(callFutures);
                callFutures.clear();
            } else {
                CHANNEL_ID_2_CALLS.put(channelId, new ConcurrentLinkedQueue<>());
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            String channelId = ctx.channel().id().asLongText();
            Queue<HttpCallFuture> callFutures = CHANNEL_ID_2_CALLS.remove(channelId);
            retryCalls(callFutures);
        }

        /**
         * 重试之前的 http call
         */
        private void retryCalls(Collection<HttpCallFuture> callFutures) {
            if (CollectionUtils.isEmpty(callFutures)) {
                return;
            }
            callFutures.forEach(callFuture -> callFuture.done(null));
        }

        @Override
        public void handleException(ChannelHandlerContext ctx, Throwable cause) {
            log.error("", cause);
        }
    }

    /**
     * http call future
     */
    private class HttpCallFuture implements Future<HttpResponse> {
        private final HttpCall httpCall;
        private final OneLock sync;
        private volatile HttpResponse httpResponse;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        public HttpCallFuture(HttpCall httpCall) {
            this.httpCall = httpCall;
            this.sync = new OneLock();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!isDone() && cancelled.compareAndSet(false, true)) {
                //仅仅释放锁
                sync.release(1);
                return true;
            }

            return false;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return sync.isDone() || isCancelled();
        }

        @Override
        public HttpResponse get() {
            //获取锁
            sync.acquire(-1);
            if (isDone()) {
                return httpResponse;
            }
            return null;
        }

        @Override
        public HttpResponse get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
            if (success) {
                if (isDone()) {
                    return httpResponse;
                }
                return null;
            }
            throw new TimeoutException();
        }

        public void done(HttpResponse httpResponse) {
            if (isDone()) {
                return;
            }
            //空即retry
            this.httpResponse = httpResponse;
            if (Objects.nonNull(this.httpResponse)) {
                this.httpResponse.setHttpRequest(httpCall.getRequest());
            }
            //释放锁
            sync.release(1);
        }
    }

}
