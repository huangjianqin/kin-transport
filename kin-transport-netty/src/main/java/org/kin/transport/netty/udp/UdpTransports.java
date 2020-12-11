package org.kin.transport.netty.udp;

/**
 * udp transports
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransports {
    public static final UdpTransports INSTANCE = new UdpTransports();

    /** server配置 */
    public final UdpTransportOption.UdpServerTransportOptionBuilder server() {
        return new UdpTransportOption.UdpServerTransportOptionBuilder();
    }

    /** client配置 */
    public final UdpTransportOption.UdpClientTransportOptionBuilder client() {
        return new UdpTransportOption.UdpClientTransportOptionBuilder();
    }
}
