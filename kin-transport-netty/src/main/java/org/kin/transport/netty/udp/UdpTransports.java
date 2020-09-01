package org.kin.transport.netty.udp;

import org.kin.transport.netty.udp.client.UdpClientTransportOption;
import org.kin.transport.netty.udp.server.UdpServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpTransports {
    public static final UdpTransports INSTANCE = new UdpTransports();

    /** server配置 */
    public final UdpServerTransportOption server() {
        return new UdpServerTransportOption();
    }

    /** client配置 */
    public final UdpClientTransportOption client() {
        return new UdpClientTransportOption();
    }
}
