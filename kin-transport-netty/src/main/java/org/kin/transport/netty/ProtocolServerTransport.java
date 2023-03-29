package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class ProtocolServerTransport<PST extends ProtocolServerTransport<PST>> extends ProtocolTransport<PST> {
    /**  */
    private ServerLifecycle lifecycle = ServerLifecycle.DEFAULT;

    //setter && getter
    public ServerLifecycle getLifecycle() {
        return lifecycle;
    }

    @SuppressWarnings("unchecked")
    public PST lifecycle(ServerLifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return (PST) this;
    }
}
