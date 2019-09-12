package org.kin.transport.netty.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.kin.transport.netty.core.AbstractSession;
import org.kin.transport.netty.core.Bytes2ProtocolTransfer;
import org.kin.transport.netty.core.common.ProtocolConstants;
import org.kin.transport.netty.core.domain.ProtocolByteBuf;
import org.kin.transport.netty.core.domain.Request;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.statistic.InOutBoundStatisicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjianqin on 2019/5/29.
 */
public class ProtocolCodec extends MessageToMessageCodec<List<ByteBuf>, ProtocolByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(ProtocolCodec.class);
    private final Bytes2ProtocolTransfer transfer;
    //true = server, false = client
    private boolean serverElseClient;

    public ProtocolCodec(Bytes2ProtocolTransfer transfer, boolean serverElseClient) {
        this.transfer = transfer;
        this.serverElseClient = serverElseClient;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolByteBuf in, List<Object> out) throws Exception {
        if (serverElseClient) {
            //server send response
            ByteBuf outByteBuf = ctx.alloc().buffer();
            ByteBuf byteBuf = in.getByteBuf();
            outByteBuf.writeInt(getRespSN(ctx.channel()));
            outByteBuf.writeBytes(byteBuf, 0, byteBuf.readableBytes());
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticResp(in.getProtocolId() + "", in.getSize());
        } else {
            //client send request
            ByteBuf outByteBuf = ctx.alloc().buffer();
            ByteBuf byteBuf = in.getByteBuf();
            outByteBuf.writeBytes(byteBuf, 0, byteBuf.readableBytes());
            out.add(outByteBuf);

            InOutBoundStatisicService.instance().statisticReq(in.getProtocolId() + "", in.getSize());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, List<ByteBuf> in, List<Object> out) throws Exception {
        /** 合并解包 */
        List<AbstractProtocol> protocols = new ArrayList<>();
        for(ByteBuf inByteBuf: in){
            if (serverElseClient) {
                Request byteBufRequest = new ProtocolByteBuf(inByteBuf);
                protocols.add(transfer.transfer(byteBufRequest));

                //server receive request
                InOutBoundStatisicService.instance()
                        .statisticReq(byteBufRequest.getProtocolId() + "", byteBufRequest.getContentSize());
            } else {
                //client 需要解析respSN
                Request byteBufRequest = new ProtocolByteBuf(inByteBuf, true);
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
}
