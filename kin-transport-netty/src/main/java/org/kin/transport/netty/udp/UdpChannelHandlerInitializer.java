package org.kin.transport.netty.udp;

import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpChannelHandlerInitializer<O extends AbstractUdpTransportOption<O>>
        extends AbstractChannelHandlerInitializer<DatagramPacket, UdpProtocolWrapper, DatagramPacket, O> {
    public UdpChannelHandlerInitializer(O transportOption) {
        super(transportOption);
    }
}
