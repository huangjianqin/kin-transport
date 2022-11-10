package org.kin.transport.netty.http.server;

import java.lang.reflect.Method;

/**
 * http请求处理方法签名异常
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public final class HandleMethodSignatureException extends RuntimeException {
    private static final long serialVersionUID = 5695362414242376082L;

    public HandleMethodSignatureException(String message) {
        super(message);
    }

    public static HandleMethodSignatureException returnTypeError(Method method) {
        return new HandleMethodSignatureException(
                String.format("http request handle method '%s's return type must be Flux or Mono, but actually is %s",
                        method.toString(), method.getReturnType()));
    }
}
