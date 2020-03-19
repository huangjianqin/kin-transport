package org.kin.transport.core;

import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.protocol.domain.Response;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
public abstract class RequestProtocol extends AbstractProtocol {
    @Override
    public void write(Response response) {

    }
}
