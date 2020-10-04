package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
public class ProtocolException extends RuntimeException {
    private static final long serialVersionUID = -425240372761536233L;

    public ProtocolException(String message) {
        super(message);
    }
}
