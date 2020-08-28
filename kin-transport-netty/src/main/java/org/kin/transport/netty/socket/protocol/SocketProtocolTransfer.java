package org.kin.transport.netty.socket.protocol;

/**
 * socket协议转换
 * bytebuf转成对应的协议类
 * <p>
 * 基于{@link ProtocolFactory}
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class SocketProtocolTransfer implements ProtocolTransfer {
    private static final SocketProtocolTransfer INSTANCE = new SocketProtocolTransfer();

    private SocketProtocolTransfer() {
    }

    public static SocketProtocolTransfer instance() {
        return INSTANCE;
    }

    @Override
    public <T extends AbstractProtocol> T transfer(Request msg) {
        AbstractProtocol protocol = ProtocolFactory.createProtocol(msg.getProtocolId());
        protocol.read(msg);
        return (T) protocol;
    }
}
