package org.kin.transport.http;

/**
 * 异步http请求回调
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
public interface HttpCallback<T> {
    /** http请求返回时触发 */
    void onReceived(HttpResponse response);

    /** http请求时异常触发 */
    void onFailure(Throwable exception);
}
