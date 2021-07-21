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
        Protocol2 inst = new Protocol2();
        inst.s = s;
        inst.b = b;
        return inst;
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
        return "Protocol2{" +
                "s='" + s + '\'' +
                ", b=" + b +
                "} " + super.toString();
    }
}
