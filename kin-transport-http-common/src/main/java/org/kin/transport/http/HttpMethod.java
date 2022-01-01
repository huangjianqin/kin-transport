package org.kin.transport.http;

import com.google.common.collect.ImmutableMap;
import org.kin.framework.utils.StringUtils;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2021/12/29
 */
public enum HttpMethod {
    /** GET */
    GET,
    /** HEAD */
    HEAD,
    /** POST */
    POST,
    /** PUT */
    PUT,
    /** PATCH */
    PATCH,
    /** DELETE */
    DELETE,
    /** OPTIONS */
    OPTIONS,
    /** TRACE */
    TRACE;

    /** key -> http method name, value -> http method enum */
    private static final Map<String, HttpMethod> MAPPINGS;

    static {
        ImmutableMap.Builder<String, HttpMethod> builder = ImmutableMap.builder();
        for (HttpMethod httpMethod : values()) {
            builder.put(httpMethod.name(), httpMethod);
        }
        MAPPINGS = builder.build();
    }

    HttpMethod() {
    }

    /**
     * 是否匹配指定http method
     */
    public boolean matches(String method) {
        return this.name().equalsIgnoreCase(method);
    }

    /**
     * 根据http method name获取{@link HttpMethod}实例
     */
    @Nullable
    public static HttpMethod getByName(@Nullable String method) {
        if (StringUtils.isBlank(method)) {
            return null;
        }
        return MAPPINGS.get(method);
    }
}
