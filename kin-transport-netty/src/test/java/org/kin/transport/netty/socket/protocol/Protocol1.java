package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 1)
public class Protocol1 extends AbstractSocketProtocol {
    private int f;

    public static Protocol1 of(int f) {
        return ProtocolFactory.createProtocol(1, f);
    }

    @Override
    public void read(SocketByteBufRequest request) {
        f = request.readInt();
    }

    @Override
    public void write(SocketByteBufResponse response) {
        response.writeInt(f);
    }

    @Override
    public String toString() {
        return super.toString() + "Protocol1{" +
                "f=" + f +
                '}';
    }
}
