package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandlerContext;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.ProtocolHandler;

import java.util.Objects;

/**
 * http client protocol handler
 *
 * @author huangjianqin
 * @date 2021/1/24
 */
class HttpClientProtocolHandler extends ProtocolHandler<HttpEntity> implements LoggerOprs {
    /** lazy init */
    private volatile HttpClient httpClient;

    @Override
    public void handle(ChannelHandlerContext ctx, HttpEntity protocol) {
        if (!(protocol instanceof HttpResponse)) {
            return;
        }

        HttpCallFuture callFuture = httpClient.getCallFuture();

        HttpResponse response = (HttpResponse) protocol;
        if (Objects.isNull(callFuture)) {
            throw new IllegalArgumentException("empty call");
        }
        callFuture.done(response);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        super.channelInactive(ctx);
        retryCall();
    }

    /**
     * 重试之前的 http call
     */
    private void retryCall() {
        HttpCallFuture callFuture = httpClient.getCallFuture();
        if (Objects.isNull(callFuture)) {
            return;
        }
        callFuture.done(null);
    }

    @Override
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
        super.handleException(ctx, cause);
        error("", cause);
        retryCall();
    }

    //setter && getter
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public String toString() {
        return "HttpClientProtocolHandler{" +
                "httpClient=" + httpClient +
                "} " + super.toString();
    }
}
