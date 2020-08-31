package org.kin.transport.netty.http;

import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.http.client.HttpClientTransportOption;
import org.kin.transport.netty.http.server.HttpServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractHttpTransportOption<IN, MSG, OUT> extends AbstractTransportOption<IN, MSG, OUT> {
    public static final AbstractHttpTransportOption INSTANCE = new AbstractHttpTransportOption() {
    };

    /** server配置 */
    public static <MSG> HttpServerTransportOption<MSG> server() {
        return new HttpServerTransportOption<>();
    }

    /** client配置 */
    public static <MSG> HttpClientTransportOption<MSG> client() {
        return new HttpClientTransportOption<>();
    }
    //------------------------------------------------------------------------------------------------------------
}
