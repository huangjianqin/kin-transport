package org.kin.transport.netty.http.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.http.AbstractHttpTransportOption;

import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerHandlerInitializer<MSG>
        extends AbstractChannelHandlerInitializer<FullHttpRequest, MSG, FullHttpResponse,
        AbstractHttpTransportOption<FullHttpRequest, MSG, FullHttpResponse>> {

    public HttpServerHandlerInitializer(HttpServerTransportOption<MSG> transportOption) {
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
