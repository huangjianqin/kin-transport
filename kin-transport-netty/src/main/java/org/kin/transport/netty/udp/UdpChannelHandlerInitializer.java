package org.kin.transport.netty.udp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.socket.DatagramPacket;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.AbstractTransportOption;

import java.util.Collection;
import java.util.Collections;

/**
 * udp channel handler初始化
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpChannelHandlerInitializer<O extends AbstractTransportOption<DatagramPacket, UdpProtocolDetails, DatagramPacket, O>>
        extends AbstractChannelHandlerInitializer<DatagramPacket, UdpProtocolDetails, DatagramPacket, O> {
    public UdpChannelHandlerInitializer(O transportOption) {
        super(transportOption);
    }

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        return Collections.emptyList();
    }
}
