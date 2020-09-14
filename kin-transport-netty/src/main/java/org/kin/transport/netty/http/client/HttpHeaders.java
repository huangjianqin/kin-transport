package org.kin.transport.netty.http.client;

import java.util.HashMap;

/**
 * @author huangjianqin
 * @date 2020/9/3
 */
public final class HttpHeaders extends HashMap<String, String> {
    /**
     * 获取头部信息
     */
    public String header(String name) {
        return getOrDefault(name, "");
    }

    /**
     * 设置头部信息
     */
    public HttpHeaders add(String name, String value) {
        put(name, value);
        return this;
    }
}
