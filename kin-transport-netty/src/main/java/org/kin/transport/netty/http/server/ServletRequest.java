package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.JSON;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.http.HttpRequestBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.client.HttpHeaders;

import java.util.*;

/**
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class ServletRequest implements ServletTransportEntity {
    /** 请求的url信息 */
    private final HttpUrl url;
    /** 请求的method */
    private final HttpMethod method;
    /** 请求的http头部信息 */
    private final HttpHeaders headers;
    /** http session */
    private HttpSession session;
    /** cookies */
    private final List<Cookie> cookies;
    /** request body */
    private final HttpRequestBody requestBody;
    /** 是否长连接 */
    private final boolean isKeepAlive;

    /** 从url和requestBody解析出来的参数 */
    private Map<String, Object> params;

    ServletRequest(HttpUrl url,
                   HttpMethod method,
                   HttpHeaders headers,
                   List<Cookie> cookies,
                   HttpRequestBody requestBody,
                   boolean isKeepAlive) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.cookies = cookies;
        this.requestBody = requestBody;
        this.isKeepAlive = isKeepAlive;
    }

    /**
     * 解析出参数map
     */
    private Map<String, Object> parseParams() {
        //默认先从url获取参数
        Map<String, Object> params = new HashMap<>();
        QueryStringDecoder decoder = new QueryStringDecoder(url.uri());
        for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
            //entry.getValue()是一个List, 只取第一个元素
            params.put(entry.getKey(), entry.getValue().get(0));
        }

        if (Objects.isNull(requestBody)) {
            return params;
        }

        //然后用body内容覆盖url的参数
        params.putAll(requestBody.getParams());
        return params;
    }

    /**
     * 获取所有参数
     * 存在可能空map, 因为requestBody仅仅支持被消费一次
     */
    public Map<String, Object> getParams() {
        Map<String, Object> params = this.params;
        if (Objects.isNull(params)) {
            //没有初始化过
            params = parseParams();
            if (CollectionUtils.isEmpty(params)) {
                params = Collections.emptyMap();
            }
            this.params = params;
        }
        return params;
    }

    /**
     * 获取string类型参数
     */
    public String getParam(String name) {
        Object value = params.get(name);
        return Objects.isNull(value) ? "" : value.toString();
    }

    /**
     * 获取short类型参数
     */
    public int getShortParam(String name) {
        String value = getParam(name);
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Short.parseShort(value);
        }
    }

    /**
     * 获取int类型参数
     */
    public int getIntParam(String name) {
        String value = getParam(name);
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 获取long类型参数
     */
    public long getLongParam(String name) {
        String value = getParam(name);
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Long.parseLong(value);
        }
    }

    /**
     * 获取float类型参数
     */
    public float getFloatParam(String name) {
        String value = getParam(name);
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Float.parseFloat(value);
        }
    }

    /**
     * 获取double类型参数
     */
    public double getDoubleParam(String name) {
        String value = getParam(name);
        if (StringUtils.isBlank(value)) {
            return 0;
        } else {
            return Double.parseDouble(value);
        }
    }

    /**
     * 获取指定类型参数
     */
    public <C> C getObjParam(String name, Class<? extends C> targetClasss) {
        Object value = params.get(name);
        if (Objects.isNull(value)) {
            return null;
        } else {
            return JSON.convert(value, targetClasss);
        }
    }


    /**
     * 获取内容
     * 存在可能空串, 因为requestBody仅仅支持被消费一次
     */
    public String getContent() {
        if (Objects.isNull(requestBody)) {
            return "";
        }
        return requestBody.getContent();
    }

    /**
     * 获取cookie内容
     */
    public String cookie(String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }

        return null;
    }

    //getter
    public HttpUrl getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpSession getSession() {
        return session;
    }

    void setSession(HttpSession session) {
        this.session = session;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public boolean isKeepAlive() {
        return isKeepAlive;
    }
}
