package org.kin.transport.netty.socket.protocol;

/**
 * 协议转换
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
@FunctionalInterface
public interface ProtocolTransfer {
    /**
     * 解析从字节数组转换成协议对象
     *
     * @param request in协议(本质上是个byteBuf的封装)
     * @param <T>     协议实现类
     * @return 协议实现类
     */
    <T extends AbstractProtocol> T transfer(Request msg);
}
