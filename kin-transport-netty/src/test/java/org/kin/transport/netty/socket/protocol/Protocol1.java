package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 1)
public class Protocol1 extends SocketProtocol {
    private int f;

    public static Protocol1 of(int f) {
        return ProtocolFactory.createProtocol(1, f);
    }

    @Override
    public void read(SocketRequestOprs request) {
        f = request.readInt();
    }

    @Override
    public void write(SocketResponseOprs response) {
        response.writeInt(f);
    }

    @Override
    public String toString() {
        return super.toString() + "Protocol1{" +
                "f=" + f +
                '}';
    }
}
