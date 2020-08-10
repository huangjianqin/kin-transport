package org.kin.transport.netty.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.kin.framework.collection.Tuple;
import org.kin.transport.netty.core.protocol.Bytes2ProtocolTransfer;
import org.kin.transport.netty.core.protocol.domain.ProtocolByteBuf;
import org.kin.transport.netty.core.protocol.domain.Request;
import org.kin.transport.netty.core.statistic.InOutBoundStatisicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author huangjianqin
 * @date 2019/5/29
 */
public class ProtocolCodec extends MessageToMessageCodec<ByteBuf, ProtocolByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(ProtocolCodec.class);
    private final Bytes2ProtocolTransfer transfer;
    /** true = server, false = client */
    private final boolean serverElseClient;
    /** 是否压缩 */
    private final boolean compression;

    public ProtocolCodec(Bytes2ProtocolTransfer transfer, boolean serverElseClient, boolean compression) {
        this.transfer = transfer;
        this.serverElseClient = serverElseClient;
        this.compression = compression;
    }

    public ProtocolCodec(Bytes2ProtocolTransfer transfer, boolean serverElseClient) {
        this(transfer, serverElseClient, false);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolByteBuf in, List<Object> out) {
        Tuple<Boolean, byte[]> tuple = getRealOutBytes(in);
        if (serverElseClient) {
            //server send response
            ByteBuf outByteBuf = ctx.alloc().buffer();

            outByteBuf.writeBoolean(tuple.first());
            outByteBuf.writeBytes(tuple.second());
            ReferenceCountUtil.retain(outByteBuf);
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticResp(in.getProtocolId() + "", in.getSize());
        } else {
            //client send request
            ByteBuf outByteBuf = ctx.alloc().buffer();

            outByteBuf.writeBoolean(tuple.first());
            outByteBuf.writeBytes(tuple.second());
            ReferenceCountUtil.retain(outByteBuf);
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticReq(in.getProtocolId() + "", in.getSize());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            boolean compression = in.readBoolean();
            if (serverElseClient) {
                in = getRealInByteBuff(in, compression);
                Request byteBufRequest = new ProtocolByteBuf(in);
                out.add(transfer.transfer(byteBufRequest));

                //server receive request
                InOutBoundStatisicService.instance()
                        .statisticReq(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
            } else {
                in = getRealInByteBuff(in, compression);
                Request byteBufRequest = new ProtocolByteBuf(in);
                out.add(transfer.transfer(byteBufRequest));

                //client receive response
                InOutBoundStatisicService.instance()
                        .statisticResp(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
            }
        } finally {
            ReferenceCountUtil.release(in);
        }
    }

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
                log.error("compress error, back >>> ", e);
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
