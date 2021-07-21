package org.kin.transport.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.kin.framework.utils.ClassUtils;
import org.kin.transport.netty.TransportProtocolTransfer;

import java.lang.reflect.Type;
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

    @SuppressWarnings("unchecked")
    private static <IN, MSG, OUT> Class<? extends IN> getInClass(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
        List<Type> types = ClassUtils.getSuperInterfacesGenericActualTypes(TransportProtocolTransfer.class, transfer.getClass());
        return (Class<? extends IN>) types.get(0);
    }

    @SuppressWarnings("unchecked")
    private static <IN, MSG, OUT> Class<? extends MSG> getMsgClass(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
        List<Type> types = ClassUtils.getSuperInterfacesGenericActualTypes(TransportProtocolTransfer.class, transfer.getClass());
        return (Class<? extends MSG>) types.get(1);
    }

    public TransportProtocolCodec(TransportProtocolTransfer<IN, MSG, OUT> transfer) {
        super(getInClass(transfer), getMsgClass(transfer));
        this.transfer = transfer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MSG in, List<Object> out) throws Exception {
        //todo 可以考虑MSG是一个列表返回, 进而减少一丢丢CPU消耗, 比如说write out 10个包, 那就要走10次netty handler 链, 如果是列表, 则仅仅需要一次
        out.addAll(transfer.encode(ctx, in));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, IN in, List<Object> out) throws Exception {
        out.addAll(transfer.decode(ctx, in));
    }
}
