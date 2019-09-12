package org.kin.transport.netty.core;

import org.kin.transport.netty.core.domain.Request;
import org.kin.transport.netty.core.protocol.AbstractProtocol;

/**
 * Created by huangjianqin on 2019/6/3.
 */
@FunctionalInterface
public interface Bytes2ProtocolTransfer {
    <T extends AbstractProtocol> T transfer(Request request);
}
