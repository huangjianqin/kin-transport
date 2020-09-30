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
public class UdpProtocolDetails {
    /** 封装的协议 */
    private SocketProtocol protocol;
    /** 目标地址 */
    private InetSocketAddress targetAddress;
    /** 发送地址 */
    private InetSocketAddress senderAddress;

    /**
     * 封装 接收到的 协议
     */
    public static UdpProtocolDetails receiverWrapper(SocketProtocol protocol, InetSocketAddress senderAddress) {
        UdpProtocolDetails details = new UdpProtocolDetails();
        details.protocol = protocol;
        details.senderAddress = senderAddress;
        return details;
    }

    /**
     * 封装 待发送的 协议
     */
    public static UdpProtocolDetails senderWrapper(SocketProtocol protocol, InetSocketAddress targetAddress) {
        UdpProtocolDetails details = new UdpProtocolDetails();
        details.protocol = protocol;
        details.targetAddress = targetAddress;
        return details;
    }

    //---------------------------------------------------------------------------------------------------------
    public SocketProtocol getProtocol() {
        return protocol;
    }

    public InetSocketAddress getTargetAddress() {
        return targetAddress;
    }

    public InetSocketAddress getSenderAddress() {
        return senderAddress;
    }
}
