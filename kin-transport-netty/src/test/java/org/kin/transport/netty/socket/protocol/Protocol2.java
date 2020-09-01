package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 2)
public class Protocol2 extends RequestProtocol {
    private String s;
    private byte b;

    public static Protocol2 of(String s, byte b) {
        return ProtocolFactory.createProtocol(2, s, b);
    }

    @Override
    public void read(SocketRequestOprs request) {
        s = request.readString();
        b = request.readByte();
    }

    @Override
    public String toString() {
        return super.toString() + "Protocol2{" +
                "s='" + s + '\'' +
                ", b=" + b +
                '}';
    }
}
