package org.kin.transport.netty.http.server;

/**
 * servlet异常类, 仅仅是描述不同
 *
 * @author huangjianqin
 * @date 2020/9/14
 */
public class ServletException extends RuntimeException {
    public ServletException(String message) {
        super(message);
    }
}
