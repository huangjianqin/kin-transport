package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 1)
public class Protocol1 extends SocketProtocol {
    private int f;

    public static Protocol1 of(int f) {
        Protocol1 inst = new Protocol1();
        inst.f = f;
        return inst;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    @Override
    public String toString() {
        return "Protocol1{" +
                "f=" + f +
                "} " + super.toString();
    }
}
