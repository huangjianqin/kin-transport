package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2020/9/9
 */
public class ClientConnectTimeoutException extends RuntimeException {
    public ClientConnectTimeoutException(String hostPort) {
        super(String.format("client connect remote server('%s') timeout", hostPort));
    }
}
