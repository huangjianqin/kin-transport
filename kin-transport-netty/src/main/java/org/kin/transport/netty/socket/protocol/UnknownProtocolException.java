package org.kin.transport.netty.socket.protocol;

/**
 * 未知协议异常
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class UnknownProtocolException extends RuntimeException {
    public UnknownProtocolException(int id) {
        super("unknow protocol '" + id + "'");
    }
}