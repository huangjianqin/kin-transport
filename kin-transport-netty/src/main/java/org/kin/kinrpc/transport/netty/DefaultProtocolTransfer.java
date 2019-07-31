package org.kin.kinrpc.transport.netty;

import org.kin.kinrpc.transport.netty.domain.Request;
import org.kin.kinrpc.transport.netty.protocol.AbstractProtocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
public class DefaultProtocolTransfer implements Bytes2ProtocolTransfer {
    private static final DefaultProtocolTransfer INSTANCE = new DefaultProtocolTransfer();

    private DefaultProtocolTransfer() {
    }

    public static DefaultProtocolTransfer instance() {
        return INSTANCE;
    }

    @Override
    public <T extends AbstractProtocol> T transfer(Request request) {
        AbstractProtocol protocol = ProtocolFactory.createProtocol(request.getProtocolId());
        protocol.read(request);
        return (T) protocol;
    }
}
