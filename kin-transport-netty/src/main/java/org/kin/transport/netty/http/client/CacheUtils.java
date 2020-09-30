package org.kin.transport.netty.http.client;

/**
 * @author huangjianqin
 * @date 2020/9/29
 */
class CacheUtils {
    /**
     * 生成缓存key
     * 相当于 url域名的hashcode的16进制字符串
     */
    static String generateCacheKey(HttpRequest httpRequest) {
        return Integer.toHexString(httpRequest.getUrl().url().toString().hashCode());
    }
}
