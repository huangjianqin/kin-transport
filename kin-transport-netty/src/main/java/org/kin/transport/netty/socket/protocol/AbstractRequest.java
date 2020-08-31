package org.kin.transport.netty.socket.protocol;

/**
 * in抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class AbstractRequest extends AbstractSocketProtocol {
    public AbstractRequest() {
    }

    public AbstractRequest(int protocolId) {
        super(protocolId);
    }

    @Override
    public final void write(SocketByteBufResponse response) {
        //do nothing
    }
}
