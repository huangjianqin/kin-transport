package org.kin.transport.netty.http.client;

import java.io.IOException;

/**
 * http请求回调
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public interface HttpCallback {
    /**
     * 失败回调
     *
     * @param httpCall  http 请求
     * @param exception 异常
     */
    void onFailure(HttpCall httpCall, Exception exception);

    /**
     * 成功回调
     *
     * @param httpCall http 请求
     * @param response http 响应
     */
    void onResponse(HttpCall httpCall, HttpResponse httpResponse) throws IOException;
}
