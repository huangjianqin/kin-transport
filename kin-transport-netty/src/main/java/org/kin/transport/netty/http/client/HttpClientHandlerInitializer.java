package org.kin.transport.netty.http.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.http.client.handler.ByteBuf2HttpRequestEncoder;
import org.kin.transport.netty.http.client.handler.HttpClientHandler;

import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public class HttpClientHandlerInitializer extends AbstractChannelHandlerInitializer {
    protected final HttpClientTransportOption transportOption;

    public HttpClientHandlerInitializer(HttpClientTransportOption transportOption) {
        this.transportOption = transportOption;
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpResponseEncoder());
        channelHandlers.add(new HttpRequestDecoder());
        channelHandlers.add(new HttpObjectAggregator(65536));
        channelHandlers.add(new HttpClientHandler(transportOption.getTransportHandler()));
        channelHandlers.add(new ByteBuf2HttpRequestEncoder());
        return channelHandlers;
    }
}
