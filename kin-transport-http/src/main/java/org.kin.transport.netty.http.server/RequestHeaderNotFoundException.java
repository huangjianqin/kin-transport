package org.kin.transport.netty.http.server;

import reactor.netty.http.server.HttpServerRequest;

/**
 * http headers不存在指定name属性异常
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public final class RequestHeaderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 829255339965761755L;

    public RequestHeaderNotFoundException(HttpServerRequest request, String header) {
        super(String.format("request header '%s' is not found in request headers '%s'", header, request.requestHeaders()));
    }
}
