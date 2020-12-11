package org.kin.transport.netty.socket;

/**
 * tcp transports
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class SocketTransports {
    public static final SocketTransports INSTANCE = new SocketTransports();

    /** server配置 */
    public final SocketTransportOption.SocketServerTransportOptionBuilder server() {
        return new SocketTransportOption.SocketServerTransportOptionBuilder();
    }

    /** client配置 */
    public final SocketTransportOption.SocketClientTransportOptionBuilder client() {
        return new SocketTransportOption.SocketClientTransportOptionBuilder();
    }
}
