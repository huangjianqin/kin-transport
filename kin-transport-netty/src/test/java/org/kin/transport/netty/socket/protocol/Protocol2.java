package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 2)
public class Protocol2 extends RequestProtocol {
    private String s;
    private byte b;

    @Override
    public void read(SocketByteBufRequest request) {
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
