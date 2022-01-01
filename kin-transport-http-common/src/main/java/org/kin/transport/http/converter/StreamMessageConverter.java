package org.kin.transport.http.converter;

import org.kin.framework.utils.Extension;
import org.kin.transport.http.HttpResponse;

import java.io.InputStream;

/**
 * 直接返回{@link java.io.InputStream}
 *
 * @author huangjianqin
 * @date 2022/1/1
 */
@Extension(value = "stream")
public class StreamMessageConverter implements MessageConverter {
    @Override
    public boolean canConvert(HttpResponse response, Class<?> respClass) {
        return InputStream.class.equals(respClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(InputStream inputStream, Class<T> respClass) {
        return (T) inputStream;
    }
}
