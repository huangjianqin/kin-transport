package org.kin.transport.netty.http.server;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;

import java.util.Collection;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
final class HttpServerHandlerInitializer
        extends AbstractChannelHandlerInitializer<FullHttpRequest, ServletTransportEntity, FullHttpResponse,
        HttpServerTransportOption> {

    public HttpServerHandlerInitializer(HttpServerTransportOption transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = setUpChannelHandlers(transportOption);

        channelHandlers.add(new HttpServerCodec());
        //通过在header增加Accept-Encoding -> gzip | deflate, netty就自动完成压缩
        channelHandlers.add(new HttpContentCompressor());
        channelHandlers.add(new HttpObjectAggregator(65536));
        channelHandlers.add(new ChunkedWriteHandler());

        return channelHandlers;
    }
}
