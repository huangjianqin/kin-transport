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
    /** 封装的协议 */
    private SocketProtocol protocol;
    /** 目标地址 */
    private InetSocketAddress targetAddress;
    /** 发送地址 */
    private InetSocketAddress senderAddress;

    /**
     * 封装 接收到的 协议
     */
    public static UdpProtocolWrapper receiverWrapper(SocketProtocol protocol, InetSocketAddress senderAddress) {
        UdpProtocolWrapper wrapper = new UdpProtocolWrapper();
        wrapper.protocol = protocol;
        wrapper.senderAddress = senderAddress;
        return wrapper;
    }

    /**
     * 封装 待发送的 协议
     */
    public static UdpProtocolWrapper senderWrapper(SocketProtocol protocol, InetSocketAddress targetAddress) {
        UdpProtocolWrapper wrapper = new UdpProtocolWrapper();
        wrapper.protocol = protocol;
        wrapper.targetAddress = targetAddress;
        return wrapper;
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
