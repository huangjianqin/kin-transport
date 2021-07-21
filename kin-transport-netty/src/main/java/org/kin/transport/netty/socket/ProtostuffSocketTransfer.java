package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.TransportProtocolTransfer;
import org.kin.transport.netty.socket.protocol.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * protocol id + data
 *
 * @author huangjianqin
 * @date 2021/7/21
 */
public class ProtostuffSocketTransfer implements TransportProtocolTransfer<ByteBuf, SocketProtocol, ByteBuf>, LoggerOprs {
    /** 避免每次序列化都重新申请Buffer空间 */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    /** true = server, false = client */
    private final boolean serverElseClient;

    public ProtostuffSocketTransfer(boolean serverElseClient) {
        this.serverElseClient = serverElseClient;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<SocketProtocol> decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        SocketRequestOprs protocolByteBuf = new SocketProtocolByteBuf(in);
        int protocolId = protocolByteBuf.getProtocolId();
        Class<? extends SocketProtocol> socketProtocolClass = ProtocolFactory.getSocketProtocolClass(protocolId);
        if (Objects.isNull(socketProtocolClass)) {
            error(String.format("can't not find SocketProtocol class with protocol id = %d", protocolId));
            //clear
            in.readBytes(in.readableBytes());
            return Collections.emptyList();
        }

        //protocol经protobuf序列化后的bytes
        byte[] protocolBytes = protocolByteBuf.readBytes();

        //反序列化
        Schema schema = Protostuffs.getSchema(socketProtocolClass);
        Object protocol = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(protocolBytes, protocol, schema);

        if (serverElseClient) {
            //server receive request
            ProtocolStatisicService.instance()
                    .statisticReq(protocolByteBuf.getProtocolId() + "", protocolByteBuf.getContentSize());
        } else {
            //client receive response
            ProtocolStatisicService.instance()
                    .statisticResp(protocolByteBuf.getProtocolId() + "", protocolByteBuf.getContentSize());
        }

        return Collections.singleton((SocketProtocol) protocol);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Collection<ByteBuf> encode(ChannelHandlerContext ctx, SocketProtocol msg) throws Exception {
        Class clazz = msg.getClass();
        Schema schema = Protostuffs.getSchema(clazz);
        byte[] data;
        try {
            data = ProtostuffIOUtil.toByteArray(msg, schema, BUFFER);
        } finally {
            BUFFER.clear();
        }

        Integer protocolId = ProtocolFactory.getProtocolId(msg.getClass());
        SocketProtocolByteBuf protocolByteBuf = new SocketProtocolByteBuf(protocolId);
        protocolByteBuf.writeBytes(data);

        if (serverElseClient) {
            //server send response
            ProtocolStatisicService.instance().statisticResp(protocolId + "", protocolByteBuf.getSize());
        } else {
            //client send request
            ProtocolStatisicService.instance().statisticReq(protocolId + "", protocolByteBuf.getSize());
        }

        return Collections.singleton(protocolByteBuf.getByteBuf());
    }
}
