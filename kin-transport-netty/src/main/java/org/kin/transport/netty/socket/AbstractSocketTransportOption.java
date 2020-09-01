package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public abstract class AbstractSocketTransportOption<O extends AbstractSocketTransportOption<O>>
        extends AbstractTransportOption<ByteBuf, AbstractSocketProtocol, ByteBuf, O> {
}
