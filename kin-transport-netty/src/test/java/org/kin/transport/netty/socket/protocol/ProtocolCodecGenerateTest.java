package org.kin.transport.netty.socket.protocol;

import javassist.NotFoundException;

/**
 * @author huangjianqin
 * @date 2020/10/4
 */
public class ProtocolCodecGenerateTest {
    public static void main(String[] args) throws NotFoundException {
        ProtocolFactory.init("org.kin.transport");

        Protocol3 protocol3 = Protocol3.of((byte) 1, (short) 2, 3, 4, 5L, 6, "7", true, VO1.of(8));
        ProtocolCodec<Protocol3> Protocol3Codec = ProtocolCodecs.codec(Protocol3.class);
        SocketResponseOprs response = Protocol3Codec.write(protocol3);
        System.out.println(response.getSize());

        SocketProtocol protocol = ProtocolFactory.createProtocol(3);
        SocketRequestOprs request = new SocketProtocolByteBuf(response.getByteBuf());
        Protocol3Codec.read(request, protocol);
        System.out.println(protocol);
    }
}
