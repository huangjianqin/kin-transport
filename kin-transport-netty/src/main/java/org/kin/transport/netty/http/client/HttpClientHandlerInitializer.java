package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.http.AbstractHttpTransportOption;

import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientHandlerInitializer<MSG>
        extends AbstractChannelHandlerInitializer<FullHttpResponse, MSG, FullHttpRequest,
        AbstractHttpTransportOption<FullHttpResponse, MSG, FullHttpRequest>> {
    public HttpClientHandlerInitializer(HttpClientTransportOption<MSG> transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpResponseEncoder());
        channelHandlers.add(new HttpRequestDecoder());
        channelHandlers.add(new HttpObjectAggregator(65536));
        return channelHandlers;
    }
}
