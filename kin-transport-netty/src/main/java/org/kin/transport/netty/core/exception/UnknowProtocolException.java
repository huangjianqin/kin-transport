package org.kin.transport.netty.core.exception;

/**
 * 未知协议异常
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class UnknowProtocolException extends RuntimeException {
    public UnknowProtocolException(int id) {
        super("unknow protocol '" + id + "'");
    }
}
