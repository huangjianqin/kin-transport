package org.kin.transport.netty.udp;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractTransportOption;

/**
 * udp 传输配置抽象
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public abstract class AbstractUdpTransportOption<O extends AbstractUdpTransportOption<O>>
        extends AbstractTransportOption<DatagramPacket, UdpProtocolWrapper, DatagramPacket, O> {
}