package org.kin.transport.netty.utils;

import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslProvider;

/**
 * @author huangjianqin
 * @date 2023/6/18
 */
public final class SslUtils {
    private SslUtils() {
    }

    /**
     * 获取ssl provider, 如果支持openssl, 则使用, 否则回退到使用jdk ssl
     */
    public static SslProvider getSslProvider() {
        if (OpenSsl.isAvailable()) {
            return SslProvider.OPENSSL_REFCNT;
        } else {
            return SslProvider.JDK;
        }
    }
}
