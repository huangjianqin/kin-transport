package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2020/9/9
 */
public class ServerBindTimeoutException extends RuntimeException {
    public ServerBindTimeoutException(String hostPort) {
        super(String.format("server('%s') bind timeout", hostPort));
    }
}
