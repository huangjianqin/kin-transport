package org.kin.transport.netty.socket.protocol;

/**
 * 协议encoder decoder
 * <p>
 * 由于javassist对泛型支持不太好, 导致该接口有些地方可以用泛型, 最后都没有用, 直接用父类
 *
 * @author huangjianqin
 * @date 2020/10/4
 */
public interface ProtocolCodec<P> {
    /**
     * bytes -> 协议
     *
     * @param request tcp请求(bytes)
     * @return 协议实例
     */
    void read(SocketRequestOprs request, SocketProtocol protocol);

    /**
     * bytes -> vo
     */
    P readVO(SocketRequestOprs request);

    /**
     * 协议 -> bytes
     *
     * @param protocol 协议实例
     * @return response bytes
     */
    SocketResponseOprs write(SocketProtocol protocol);

    /**
     * vo -> bytes
     */
    void writeVO(P vo, SocketResponseOprs response);
}
