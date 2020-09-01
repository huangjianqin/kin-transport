package org.kin.transport.netty.udp;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.udp.client.UdpClientTransportOption;
import org.kin.transport.netty.udp.server.UdpServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public abstract class AbstractUdpTransportOption<O extends AbstractUdpTransportOption<O>>
        extends AbstractTransportOption<DatagramPacket, UdpProtocolWrapper, DatagramPacket, O> {
    public static final AbstractUdpTransportOption INSTANCE = new AbstractUdpTransportOption() {
    };

    /** server配置 */
    public UdpServerTransportOption server() {
        return new UdpServerTransportOption();
    }

    /** client配置 */
    public UdpClientTransportOption client() {
        return new UdpClientTransportOption();
    }

    //----------------------------------------------------------------------------------------------------------------

}
