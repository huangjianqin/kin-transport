package org.kin.transport.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

/**
 * 传输层 <-> 协议层 数据转换逻辑具体实现
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public interface TransportProtocolTransfer<IN, MSG, OUT> {
    /**
     * 解析传输层数据
     * 保证IN已被release
     */
    Collection<MSG> decode(ChannelHandlerContext ctx, IN in) throws Exception;

    /**
     * 编码协议层数据
     */
    Collection<OUT> encode(ChannelHandlerContext ctx, MSG msg) throws Exception;
}
