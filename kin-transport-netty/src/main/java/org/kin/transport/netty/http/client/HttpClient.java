package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.Client;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 真正的http client, 用于处理底层传输
 *
 * @author huangjianqin
 * @date 2020/9/8
 */
class HttpClient extends Client<HttpEntity> {
    /**
     * client一次仅能处理一次req resp, 发送过req的client会认为是busy, 等待resp后才设置为空闲
     * 这样子设置的目的在于可以复用channel, 不用老是创建新连接
     */
    private volatile HttpCallFuture callFuture;

    public HttpClient(AbstractTransportOption<?, ?, ?, ?> transportOption, ChannelHandlerInitializer<?, ?, ?> channelHandlerInitializer) {
        super(transportOption, channelHandlerInitializer);
    }

    //-------------------------------------------------------------------------------------------------------------

    /**
     * http请求的统一调用方法
     */
    HttpCallFuture request(HttpCall httpCall) {
        HttpCallFuture httpCallFuture = new HttpCallFuture(httpCall, this);
        if (!isActive() || Objects.nonNull(this.callFuture)) {
            httpCallFuture.done(null);
            return httpCallFuture;
        }
        this.callFuture = httpCallFuture;

        ChannelFuture channelFuture = channel.writeAndFlush(httpCall.getRequest());
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                httpCallFuture.done(null);
            }
        });
        return httpCallFuture;
    }

    @Override
    public boolean sendAndFlush(HttpEntity httpEntity, ChannelFutureListener... listeners) {
        throw new UnsupportedOperationException("please use 'request(httpCall)' method");
    }

    @Override
    public boolean sendWithoutFlush(HttpEntity httpEntity, ChannelFutureListener... listeners) {
        throw new UnsupportedOperationException("please use 'request(httpCall)' method");
    }

    @Override
    public boolean sendAndScheduleFlush(HttpEntity httpEntity, int time, TimeUnit timeUnit, ChannelFutureListener... listeners) {
        throw new UnsupportedOperationException("please use 'request(httpCall)' method");
    }

    /**
     * 获取future
     */
    public HttpCallFuture getCallFuture() {
        return callFuture;
    }

    /**
     * 处理完一次REQ-RESP, 设置HttpCallFuture=null
     */
    public void free() {
        callFuture = null;
    }
}
