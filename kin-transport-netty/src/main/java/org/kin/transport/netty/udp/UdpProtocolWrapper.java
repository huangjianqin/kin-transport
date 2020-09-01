package org.kin.transport.netty.udp;

import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.net.InetSocketAddress;

/**
 * udp传输层与协议层的交互数据
 * 对{@link SocketProtocol}的再封装
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpProtocolWrapper {
    private SocketProtocol protocol;
    private InetSocketAddress targetAddress;

    public UdpProtocolWrapper(SocketProtocol protocol) {
        this.protocol = protocol;
    }

    public UdpProtocolWrapper(SocketProtocol protocol, InetSocketAddress targetAddress) {
        this.protocol = protocol;
        this.targetAddress = targetAddress;
    }

    public SocketProtocol getProtocol() {
        return protocol;
    }

    public InetSocketAddress getTargetAddress() {
        return targetAddress;
    }
}
