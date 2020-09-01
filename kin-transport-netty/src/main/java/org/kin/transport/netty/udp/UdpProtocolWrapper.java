package org.kin.transport.netty.udp;

import org.kin.transport.netty.socket.protocol.AbstractSocketProtocol;

import java.net.InetSocketAddress;

/**
 * udp传输层与协议层的交互数据
 * 对{@link AbstractSocketProtocol}的再封装
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class UdpProtocolWrapper {
    private AbstractSocketProtocol protocol;
    private InetSocketAddress targetAddress;

    public UdpProtocolWrapper(AbstractSocketProtocol protocol) {
        this.protocol = protocol;
    }

    public UdpProtocolWrapper(AbstractSocketProtocol protocol, InetSocketAddress targetAddress) {
        this.protocol = protocol;
        this.targetAddress = targetAddress;
    }

    public AbstractSocketProtocol getProtocol() {
        return protocol;
    }

    public InetSocketAddress getTargetAddress() {
        return targetAddress;
    }
}
