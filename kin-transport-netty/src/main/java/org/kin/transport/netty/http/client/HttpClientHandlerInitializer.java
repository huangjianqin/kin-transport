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
class HttpClientHandlerInitializer
        extends AbstractChannelHandlerInitializer<FullHttpResponse, HttpEntity, FullHttpRequest,
        HttpClientTransportOption> {
    HttpClientHandlerInitializer(HttpClientTransportOption transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpClientCodec());
        //客户端没必要压缩数据
        channelHandlers.add(new HttpContentDecompressor());
        //客户端最大发送/接受1m的数据
        channelHandlers.add(new HttpObjectAggregator(1024 * 1024));
        return channelHandlers;
    }
}
