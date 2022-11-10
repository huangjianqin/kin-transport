package org.kin.transport.netty.http.server;

import reactor.netty.http.server.HttpServerRequest;

/**
 * http url不存在指定query name参数异常
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public final class RequestParamNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -1815215791692850965L;

    public RequestParamNotFoundException(HttpServerRequest request, String queryName) {
        super(String.format("request param '%s' is not found from url '%s'", queryName, request.uri()));
    }
}
