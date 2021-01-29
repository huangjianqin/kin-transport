package org.kin.transport.netty.socket.protocol;

import io.netty.buffer.Unpooled;

/**
 * @author huangjianqin
 * @date 2021/1/28
 */
public class SocketProtocolByteBufTest {
    public static void main(String[] args) {
        SocketProtocolByteBuf protocolByteBuf = new SocketProtocolByteBuf(Unpooled.buffer());

        protocolByteBuf.writeVarInt32(0);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarInt32() == 0);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarInt32(1);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarInt32() == 1);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarInt32(-1);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarInt32() == -1);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarInt32(Integer.MAX_VALUE);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarInt32() == Integer.MAX_VALUE);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarInt32(Integer.MIN_VALUE);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarInt32() == Integer.MIN_VALUE);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarLong64(Long.MAX_VALUE);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarLong64() == Long.MAX_VALUE);
        System.out.println("----------------------------");

        protocolByteBuf.writeVarLong64(Long.MIN_VALUE);
        System.out.println(protocolByteBuf.getSize());
        System.out.println(protocolByteBuf.readVarLong64() == Long.MIN_VALUE);
        System.out.println("----------------------------");
    }
}
