package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class ProtocolServerTransport<PST extends ProtocolServerTransport<PST>> extends ProtocolTransport<PST> {
    /**  */
    private ServerObserver observer = ServerObserver.DEFAULT;

    //setter && getter
    public ServerObserver getObserver() {
        return observer;
    }

    @SuppressWarnings("unchecked")
    public PST observer(ServerObserver observer) {
        this.observer = observer;
        return (PST) this;
    }
}
