package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 2)
public class Protocol2 extends SocketProtocol {
    private String s;
    private byte b;

    public static Protocol2 of(String s, byte b) {
        return ProtocolFactory.createProtocol(2, s, b);
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public byte getB() {
        return b;
    }

    public void setB(byte b) {
        this.b = b;
    }

    @Override
    public String toString() {
        return super.toString() + "Protocol2{" +
                "s='" + s + '\'' +
                ", b=" + b +
                '}';
    }
}
