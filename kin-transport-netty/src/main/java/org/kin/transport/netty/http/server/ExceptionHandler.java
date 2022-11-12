package org.kin.transport.netty.http.server;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import javax.annotation.Nonnull;

/**
 * 异常统一处理
 *
 * @author huangjianqin
 * @date 2022/11/12
 */
@FunctionalInterface
public interface ExceptionHandler<T extends Throwable> {
    /**
     * 异常处理逻辑
     *
     * @param request   http request
     * @param throwable 异常
     * @return response message
     */
    @Nonnull
    Mono<String> onException(HttpServerRequest request, T throwable);
}
