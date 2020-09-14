package org.kin.transport.netty.http.server;

/**
 * server session id生成
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
@FunctionalInterface
public interface SessionIdGenerator {
    /**
     * @param jvmRoute server jvm 唯一标识
     */
    String generate(String jvmRoute);
}
