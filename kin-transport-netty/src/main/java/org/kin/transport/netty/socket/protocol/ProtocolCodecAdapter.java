package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2020/10/6
 */
public class ProtocolCodecAdapter<P> implements ProtocolCodec<P> {
    @Override
    public void read(SocketRequestOprs request, SocketProtocol protocol) {
        throw new UnsupportedOperationException();
    }

    @Override
    public P readVO(SocketRequestOprs request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketResponseOprs write(SocketProtocol protocol) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeVO(P vo, SocketResponseOprs response) {
        throw new UnsupportedOperationException();
    }
}
