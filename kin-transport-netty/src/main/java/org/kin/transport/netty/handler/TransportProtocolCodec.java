package org.kin.transport.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.util.List;

/**
 * 传输层 <-> 协议层 数据转换
 *
 * @author huangjianqin
 * @date 2019/5/29
 */
public class TransportProtocolCodec<IN, MSG, OUT> extends MessageToMessageCodec<IN, MSG> {
    /** 协议转换 */
    private final TransportProtocolTransfer<IN, MSG, OUT> transfer;

    public TransportProtocolCodec(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
        super(transfer.getInClass(), transfer.getMsgClass());
        this.transfer = transfer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MSG in, List<Object> out) throws Exception {
        out.addAll(transfer.encode(ctx, in));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, IN in, List<Object> out) throws Exception {
        out.addAll(transfer.decode(ctx, in));
    }
}
