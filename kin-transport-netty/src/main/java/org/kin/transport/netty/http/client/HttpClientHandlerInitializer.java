package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;

import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientHandlerInitializer
        extends AbstractChannelHandlerInitializer<FullHttpResponse, HttpEntity, FullHttpRequest,
        HttpClientTransportOption> {
    public HttpClientHandlerInitializer(HttpClientTransportOption transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpResponseDecoder());
        channelHandlers.add(new HttpRequestEncoder());
        channelHandlers.add(new HttpObjectAggregator(65536));
        return channelHandlers;
    }
}
