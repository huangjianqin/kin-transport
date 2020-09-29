package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * socket协议转换
 * bytebuf <-> 协议类
 * <p>
 * 基于{@link ProtocolFactory}
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class SocketTransfer implements TransportProtocolTransfer<ByteBuf, SocketProtocol, ByteBuf>, LoggerOprs {
    /** true = server, false = client */
    private final boolean serverElseClient;

    public SocketTransfer(boolean serverElseClient) {
        this.serverElseClient = serverElseClient;
    }

    /**
     * 将ProtocolByteBuf解析为AbstractProtocol
     */
    private SocketProtocol parseProtocolByteBuf(SocketRequestOprs byteBufRequest) {
        SocketProtocol protocol = ProtocolFactory.createProtocol(byteBufRequest.getProtocolId());
        protocol.read(byteBufRequest);
        return protocol;
    }

    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, ByteBuf in) {
        SocketRequestOprs byteBufRequest = new SocketProtocolByteBuf(in);
        SocketProtocol protocol = parseProtocolByteBuf(byteBufRequest);
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
    public Collection<ByteBuf> encode(ChannelHandlerContext ctx, SocketProtocol msg) {
        List<ByteBuf> out = new ArrayList<>();

        SocketProtocolByteBuf protocolByteBuf = (SocketProtocolByteBuf) msg.write();
        ByteBuf outByteBuf = protocolByteBuf.getByteBuf();
        out.add(outByteBuf);

        ReferenceCountUtil.retain(outByteBuf);

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
    public Class<SocketProtocol> getMsgClass() {
        return SocketProtocol.class;
    }
}
