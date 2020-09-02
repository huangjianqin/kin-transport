package org.kin.transport.netty.socket.protocol;

/**
 * socket in抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class SocketRequest extends SocketProtocol {
    public SocketRequest() {
    }

    public SocketRequest(int protocolId) {
        super(protocolId);
    }

    @Override
    public final void write(SocketResponseOprs response) {
        //do nothing
    }
}
