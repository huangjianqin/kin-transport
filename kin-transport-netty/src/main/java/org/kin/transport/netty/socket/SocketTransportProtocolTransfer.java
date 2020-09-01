package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.kin.framework.collection.Tuple;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.AbstractTransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.*;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * socket协议转换
 * bytebuf转成对应的协议类
 * <p>
 * 基于{@link ProtocolFactory}
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class SocketTransportProtocolTransfer
        extends AbstractTransportProtocolTransfer<ByteBuf, AbstractSocketProtocol, ByteBuf>
        implements LoggerOprs {
    /** true = server, false = client */
    private final boolean serverElseClient;

    public SocketTransportProtocolTransfer(boolean compression, boolean serverElseClient) {
        super(compression);
        this.serverElseClient = serverElseClient;
    }

    /**
     * 将ProtocolByteBuf解析为AbstractProtocol
     */
    private AbstractSocketProtocol parseProtocolByteBuf(SocketByteBufRequest byteBufRequest) {
        AbstractSocketProtocol protocol = ProtocolFactory.createProtocol(byteBufRequest.getProtocolId());
        protocol.read(byteBufRequest);
        return protocol;
    }

    @Override
    public Collection<AbstractSocketProtocol> decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        boolean compression = in.readBoolean();
        in = getRealInByteBuff(in, compression);
        SocketByteBufRequest byteBufRequest = new ProtocolByteBuf(in);
        AbstractSocketProtocol protocol = parseProtocolByteBuf(byteBufRequest);
        if (serverElseClient) {
            //server receive request
            ProtocolStatisicService.instance()
                    .statisticReq(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
        } else {
            //client receive response
            ProtocolStatisicService.instance()
                    .statisticResp(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
        }

        return Collections.singleton(protocol);
    }

    @Override
    public Collection<ByteBuf> encode(ChannelHandlerContext ctx, AbstractSocketProtocol msg) throws Exception {
        List<ByteBuf> out = new ArrayList<>();

        ProtocolByteBuf protocolByteBuf = (ProtocolByteBuf) msg.write();
        Tuple<Boolean, byte[]> tuple = getRealOutBytes(protocolByteBuf);
        ByteBuf outByteBuf = ctx.alloc().buffer();

        outByteBuf.writeBoolean(tuple.first());
        outByteBuf.writeBytes(tuple.second());
        ReferenceCountUtil.retain(outByteBuf);
        out.add(outByteBuf);

        if (serverElseClient) {
            //server send response
            ProtocolStatisicService.instance().statisticResp(protocolByteBuf.getProtocolId() + "", protocolByteBuf.getSize());
        } else {
            //client send request
            ProtocolStatisicService.instance().statisticReq(protocolByteBuf.getProtocolId() + "", protocolByteBuf.getSize());
        }

        return out;
    }

    @Override
    public Class<ByteBuf> getInClass() {
        return ByteBuf.class;
    }

    @Override
    public Class<AbstractSocketProtocol> getMsgClass() {
        return AbstractSocketProtocol.class;
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * 获取真正发送的bytes
     * 如果需要压缩, 则压缩, 压缩失败则回退到不压缩
     */
    private Tuple<Boolean, byte[]> getRealOutBytes(ProtocolByteBuf in) {
        ByteBuf byteBuf = in.getByteBuf();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        if (this.compression) {
            try {
                bytes = Snappy.compress(bytes);
                return new Tuple<>(true, bytes);
            } catch (Exception e) {
                log().error("compress error, back >>> ", e);
            }
        }

        //如果压缩失败, 则回退到不压缩
        return new Tuple<>(false, bytes);
    }

    /**
     * 获取真正接收到的bytes
     * 根据发送过来的压缩字段, 选择解压后返回或者直接返回
     * 不支持回退, 因为支持回退就会存在数据错乱, 直接报错就好
     */
    private ByteBuf getRealInByteBuff(ByteBuf inByteBuf, boolean compression) throws IOException {
        if (compression) {
            byte[] bytes = new byte[inByteBuf.readableBytes()];
            inByteBuf.readBytes(bytes);
            bytes = Snappy.uncompress(bytes);
            inByteBuf = Unpooled.buffer();
            inByteBuf.writeBytes(bytes);
        }

        return inByteBuf;
    }
}
