package org.kin.transport.netty.http;

import org.kin.transport.netty.http.client.HttpClientTransportOption;
import org.kin.transport.netty.http.server.HttpServerTransportOption;

/**
 * http transports
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class HttpTransports {
    public static final HttpTransports INSTANCE = new HttpTransports();

    /** server配置 */
    public final <MSG> HttpServerTransportOption<MSG> server(Class<MSG> msgClass) {
        return new HttpServerTransportOption<>();
    }

    /** client配置 */
    public final <MSG> HttpClientTransportOption<MSG> client(Class<MSG> msgClass) {
        return new HttpClientTransportOption<>();
    }
}
