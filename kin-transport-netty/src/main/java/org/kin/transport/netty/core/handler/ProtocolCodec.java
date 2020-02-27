package org.kin.transport.netty.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.kin.framework.collection.Tuple;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.core.AbstractSession;
import org.kin.transport.netty.core.Bytes2ProtocolTransfer;
import org.kin.transport.netty.core.common.ProtocolConstants;
import org.kin.transport.netty.core.domain.ProtocolByteBuf;
import org.kin.transport.netty.core.domain.Request;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.statistic.InOutBoundStatisicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author huangjianqin
 * @date 2019/5/29
 */
public class ProtocolCodec extends MessageToMessageCodec<List<ByteBuf>, ProtocolByteBuf> {
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
    protected void encode(ChannelHandlerContext ctx, ProtocolByteBuf in, List<Object> out) throws Exception {
        Tuple<Boolean, byte[]> tuple = getRealOutBytes(in);
        if (serverElseClient) {
            //server send response
            ByteBuf outByteBuf = ctx.alloc().buffer();

            outByteBuf.writeBoolean(tuple.first());
            outByteBuf.writeInt(getRespSN(ctx.channel()));
            outByteBuf.writeBytes(tuple.second());
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticResp(in.getProtocolId() + "", in.getSize());
        } else {
            //client send request
            ByteBuf outByteBuf = ctx.alloc().buffer();

            outByteBuf.writeBoolean(tuple.first());
            outByteBuf.writeBytes(tuple.second());
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticReq(in.getProtocolId() + "", in.getSize());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, List<ByteBuf> in, List<Object> out) throws Exception {
        /** 合并解包 */
        List<AbstractProtocol> protocols = new ArrayList<>();
        for (ByteBuf inByteBuf : in) {
            boolean compression = inByteBuf.readBoolean();
            if (serverElseClient) {
                inByteBuf = getRealInByteBuff(inByteBuf, compression);
                Request byteBufRequest = new ProtocolByteBuf(inByteBuf);
                protocols.add(transfer.transfer(byteBufRequest));

                //server receive request
                InOutBoundStatisicService.instance()
                        .statisticReq(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
            } else {
                //client 需要解析respSN
                int respSN = inByteBuf.readInt();
                inByteBuf = getRealInByteBuff(inByteBuf, compression);
                Request byteBufRequest = new ProtocolByteBuf(inByteBuf, respSN);
                protocols.add(transfer.transfer(byteBufRequest));

                //client receive response
                InOutBoundStatisicService.instance()
                        .statisticResp(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
            }
        }
        out.add(protocols);
    }

    private int getRespSN(Channel channel) {
        AbstractSession session = ProtocolConstants.session(channel);
        if (session != null) {
            return session.getRespSN();
        }
        return -1;
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
                ExceptionUtils.log(e, "compress error, back >>> {}");
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
