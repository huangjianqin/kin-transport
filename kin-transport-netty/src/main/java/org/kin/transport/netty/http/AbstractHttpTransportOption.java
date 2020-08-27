package org.kin.transport.netty.http;

import org.kin.transport.netty.AbstractTransportOption;
import org.kin.transport.netty.http.client.HttpClientTransportOption;
import org.kin.transport.netty.http.server.HttpServerTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractHttpTransportOption extends AbstractTransportOption {
    public static final AbstractHttpTransportOption INSTANCE = new AbstractHttpTransportOption() {
    };

    /** server配置 */
    public static HttpServerTransportOption server() {
        return new HttpServerTransportOption();
    }

    /** client配置 */
    public static HttpClientTransportOption client() {
        return new HttpClientTransportOption();
    }
    //------------------------------------------------------------------------------------------------------------
}
