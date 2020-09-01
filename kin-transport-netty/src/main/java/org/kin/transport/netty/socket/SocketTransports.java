package org.kin.transport.netty.socket;

import org.kin.transport.netty.socket.client.SocketClientTransportOption;
import org.kin.transport.netty.socket.server.SocketServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class SocketTransports {
    public static final SocketTransports INSTANCE = new SocketTransports();

    /** server配置 */
    public final SocketServerTransportOption server() {
        return new SocketServerTransportOption();
    }

    /** client配置 */
    public final SocketClientTransportOption client() {
        return new SocketClientTransportOption();
    }
}
