package org.kin.transport.http.converter;

import org.kin.framework.utils.StringUtils;
import org.kin.transport.http.HttpHeaders;
import org.kin.transport.http.HttpResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author huangjianqin
 * @date 2022/1/1
 */
public abstract class AbstractHttpMessageConverter implements MessageConverter {
    private final List<String> supportedMediaTypes;

    public AbstractHttpMessageConverter() {
        this.supportedMediaTypes = Collections.emptyList();
    }

    public AbstractHttpMessageConverter(String... supportedMediaTypes) {
        this.supportedMediaTypes = Arrays.asList(supportedMediaTypes);
    }

    @Override
    public boolean canConvert(HttpResponse response, Class<?> respClass) {
        if (!isSupport(respClass)) {
            return false;
        }
        HttpHeaders headers = response.getHeaders();
        String contentType = headers.getContentType();
        if (StringUtils.isBlank(contentType)) {
            return true;
        }

        for (String supportedMediaType : supportedMediaTypes) {
            if (contentType.contains(supportedMediaType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 是否支持反序列化成指定类
     */
    protected abstract boolean isSupport(Class<?> respClass);
}
