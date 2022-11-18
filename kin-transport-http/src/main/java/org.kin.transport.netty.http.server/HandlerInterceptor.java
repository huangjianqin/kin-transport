package org.kin.transport.netty.http.server;

import reactor.core.publisher.Mono;

/**
 * http handler拦截器
 *
 * @author huangjianqin
 * @date 2022/11/10
 * @see HttpRequestHandler
 */
public interface HandlerInterceptor {
    /**
     * http request handler执行前逻辑处理
     * <p>
     * 注意: response仅仅支持send一次, 如果在postHandle方法中调用send相关方法并且subscribe, 那么该response就会执行响应,
     *
     * @param chain interceptor执行链
     * @return 返回值要么是{@link InterceptorChain#next()}, 继续走interceptor链, 最后由handler执行, 或者其他Publisher, 那么就中断interceptor链
     */
    default Mono<Void> preHandle(InterceptorChain chain) {
        return chain.next();
    }
}
