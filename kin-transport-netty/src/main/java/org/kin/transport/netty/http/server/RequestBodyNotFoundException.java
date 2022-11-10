package org.kin.transport.netty.http.server;

import reactor.netty.http.server.HttpServerRequest;

/**
 * http request body不存在异常
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public final class RequestBodyNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5898636754943520013L;

    public RequestBodyNotFoundException(HttpServerRequest request) {
        super(String.format("request body is not found in '%s'", request));
    }
}
