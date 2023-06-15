package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class AdvancedClientTransport<ACT extends AdvancedClientTransport<ACT>> extends AdvancedTransport<ACT> {
    @SuppressWarnings("rawtypes")
    private ClientObserver observer = ClientObserver.DEFAULT;

    //setter && getter
    @SuppressWarnings("rawtypes")
    public ClientObserver getObserver() {
        return observer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ACT observer(ClientObserver observer) {
        this.observer = observer;
        return (ACT) this;
    }
}
