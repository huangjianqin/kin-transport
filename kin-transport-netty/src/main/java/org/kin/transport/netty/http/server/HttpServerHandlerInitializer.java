package org.kin.transport.netty.http.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.kin.transport.netty.ChannelHandlerInitializer;
import org.kin.transport.netty.http.server.handler.ByteBuf2HttpResponseEncoder;
import org.kin.transport.netty.http.server.handler.HttpServerHandler;

import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public class HttpServerHandlerInitializer implements ChannelHandlerInitializer {
    private final HttpServerTransportOption transportOption;

    public HttpServerHandlerInitializer(HttpServerTransportOption transportOption) {
        this.transportOption = transportOption;
    }

    @Override
    public ChannelHandler[] getChannelHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpResponseEncoder());
        channelHandlers.add(new HttpRequestDecoder());
        channelHandlers.add(new HttpObjectAggregator(65536));
        channelHandlers.add(new HttpServerHandler(transportOption.getTransportHandler()));
        channelHandlers.add(new ByteBuf2HttpResponseEncoder());

        return channelHandlers.toArray(new ChannelHandler[0]);
    }
}