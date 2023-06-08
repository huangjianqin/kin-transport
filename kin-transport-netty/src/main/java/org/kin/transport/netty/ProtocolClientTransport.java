package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class ProtocolClientTransport<PCT extends ProtocolClientTransport<PCT>> extends ProtocolTransport<PCT> {
    @SuppressWarnings("rawtypes")
    private ClientObserver observer = ClientObserver.DEFAULT;

    //setter && getter
    @SuppressWarnings("rawtypes")
    public ClientObserver getObserver() {
        return observer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public PCT observer(ClientObserver observer) {
        this.observer = observer;
        return (PCT) this;
    }
}
