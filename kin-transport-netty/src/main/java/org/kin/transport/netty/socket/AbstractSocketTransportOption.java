package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.socket.client.SocketClientTransportOption;
import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;
import org.kin.transport.netty.socket.server.SocketServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/21
 */
public abstract class AbstractSocketTransportOption extends AbstractTransportOption<ByteBuf, AbstractSocketProtocol, ByteBuf> {
    public static final AbstractSocketTransportOption INSTANCE = new AbstractSocketTransportOption() {
    };

    /** server配置 */
    public static SocketServerTransportOption server() {
        return new SocketServerTransportOption();
    }

    /** client配置 */
    public static SocketClientTransportOption client() {
        return new SocketClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------
}
