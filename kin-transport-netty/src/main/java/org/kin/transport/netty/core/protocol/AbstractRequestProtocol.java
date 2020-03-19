package org.kin.transport.netty.core.protocol;

import org.kin.transport.netty.core.protocol.domain.Response;

/**
 *
 * @author huangjianqin
 * @date 2019/5/30
 */
public abstract class AbstractRequestProtocol extends AbstractProtocol {
    public AbstractRequestProtocol() {
    }

    public AbstractRequestProtocol(int protocolId) {
        super(protocolId);
    }

    @Override
    public final void write(Response response) {
        //do nothing
    }
}
