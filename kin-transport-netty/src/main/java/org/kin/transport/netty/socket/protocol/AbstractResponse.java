package org.kin.transport.netty.socket.protocol;

/**
 * out抽象
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class AbstractResponse extends AbstractSocketProtocol {
    public AbstractResponse() {
    }

    public AbstractResponse(int protocolId) {
        super(protocolId);
    }

    @Override
    public final void read(SocketByteBufRequest request) {
        //do nothing
    }
}