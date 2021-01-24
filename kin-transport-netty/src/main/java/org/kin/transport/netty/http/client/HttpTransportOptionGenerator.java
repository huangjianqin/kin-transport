package org.kin.transport.netty.http.client;

/**
 * @author huangjianqin
 * @date 2021/1/24
 */
@FunctionalInterface
interface HttpTransportOptionGenerator {
    /**
     * 构建http传输配置
     *
     * @return
     */
    HttpClientTransportOption generate();
}
