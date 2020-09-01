package org.kin.transport.netty.socket.protocol;

/**
 * out抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class SocketResponse extends SocketProtocol {
    public SocketResponse() {
    }

    public SocketResponse(int protocolId) {
        super(protocolId);
    }

    @Override
    public final void read(SocketRequestOprs request) {
        //do nothing
    }
}