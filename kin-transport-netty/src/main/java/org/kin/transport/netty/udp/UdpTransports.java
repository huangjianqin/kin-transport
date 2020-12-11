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
        return UdpTransportOption.server();
    }

    /** client配置 */
    public final UdpTransportOption.UdpServerTransportOptionBuilder client() {
        return UdpTransportOption.client();
    }
}
