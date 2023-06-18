package org.kin.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class AdvancedClientTransport<ACT extends AdvancedClientTransport<ACT>> extends AdvancedTransport<ACT> {
    private static final Logger log = LoggerFactory.getLogger(AdvancedClientTransport.class);

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
