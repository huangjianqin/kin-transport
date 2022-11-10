package org.kin.transport.netty.http.server;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.List;

/**
 * http 请求handler
 * @author huangjianqin
 * @date 2022/11/9
 */
public interface HttpRequestHandler {
    /**
     * http 请求处理逻辑
     *
     * @param request  {@link HttpServerRequest}
     * @param response {@link HttpServerResponse}
     * @return signal
     */
    Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response);

    /**
     * @return http request method
     * @see RequestMethod
     */
    List<RequestMethod> methods();
}
