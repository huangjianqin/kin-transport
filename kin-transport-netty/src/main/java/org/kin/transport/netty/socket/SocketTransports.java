package org.kin.transport.netty.socket;

import org.kin.transport.netty.socket.client.SocketClientTransportOption;
import org.kin.transport.netty.socket.server.SocketServerTransportOption;

/**
 * tcp transports
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class SocketTransports {
    public static final SocketTransports INSTANCE = new SocketTransports();

    /** server配置 */
    public final SocketServerTransportOption.SocketServerTransportOptionBuilder server() {
        return SocketServerTransportOption.builder();
    }

    /** client配置 */
    public final SocketClientTransportOption.SocketClientTransportOptionBuilder client() {
        return SocketClientTransportOption.builder();
    }
}
