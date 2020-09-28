package org.kin.transport.netty.http.server;

/**
 * 统一servlet异常类, 仅仅是描述不同
 *
 * @author huangjianqin
 * @date 2020/9/14
 */
public class ServletException extends RuntimeException {
    private static final long serialVersionUID = 8908415136503483030L;

    public ServletException(String message) {
        super(message);
    }
}
