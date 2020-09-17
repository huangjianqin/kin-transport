package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpMethod;
import org.kin.transport.netty.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提供给开发者实现的抽象类
 *
 * @author huangjianqin
 * @date 2020/9/14
 */
public abstract class AbstractServlet implements Servlet {
    @Override
    public final void service(ServletRequest request, ServletResponse response) {
        HttpMethod method = request.getMethod();
        if (HttpMethod.GET.equals(method)) {
            handleReturn(doGet(request, response), response);
        } else if (HttpMethod.POST.equals(method)) {
            handleReturn(doPost(request, response), response);
        } else if (HttpMethod.DELETE.equals(method)) {
            doDelete(request, response);
        } else if (HttpMethod.PUT.equals(method)) {
            doPut(request, response);
        }

        response.setStatusCode(ServletResponse.SC_OK);
    }

    /**
     * 处理servlet处理方法的返回值
     */
    private void handleReturn(Object returnObj, ServletResponse response) {
        if (returnObj instanceof String) {
            response.setResponseBody(MediaType.PLAIN_TEXT.toResponseBody(returnObj.toString(), StandardCharsets.UTF_8));
        } else if (returnObj instanceof Map) {
            Map<String, Object> respContent =
                    ((Map<Object, Object>) returnObj).entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
            response.setResponseBody(MediaType.JSON.toResponseBody(respContent, StandardCharsets.UTF_8));
        }
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * 处理get请求
     */
    protected Object doGet(ServletRequest request, ServletResponse response) {
        return null;
    }

    /**
     * 处理post请求
     */
    protected Object doPost(ServletRequest request, ServletResponse response) {
        return null;
    }

    /**
     * 处理delete请求
     */
    protected void doDelete(ServletRequest request, ServletResponse response) {
    }

    /**
     * 处理put请求
     */
    protected void doPut(ServletRequest request, ServletResponse response) {
    }
}
