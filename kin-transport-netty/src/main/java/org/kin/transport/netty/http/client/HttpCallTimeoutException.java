package org.kin.transport.netty.http.client;

/**
 * http call 超时异常
 *
 * @author huangjianqin
 * @date 2020/9/9
 */
public class HttpCallTimeoutException extends RuntimeException {
    public HttpCallTimeoutException(String message) {
        super(message);
    }
}
