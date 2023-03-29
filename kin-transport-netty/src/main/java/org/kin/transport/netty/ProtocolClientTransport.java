package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class ProtocolClientTransport<PCT extends ProtocolClientTransport<PCT>> extends ProtocolTransport<PCT> {
    private ClientLifecycle lifecycle = ClientLifecycle.DEFAULT;

    //setter && getter
    public ClientLifecycle getLifecycle() {
        return lifecycle;
    }

    @SuppressWarnings("unchecked")
    public PCT lifecycle(ClientLifecycle lifecycle) {
        this.lifecycle = lifecycle;
        return (PCT) this;
    }
}
