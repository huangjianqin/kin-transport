package org.kin.transport.http.converter;

import com.google.common.io.ByteStreams;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.framework.utils.JSON;
import org.kin.transport.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 基于jackson
 *
 * @author huangjianqin
 * @date 2022/1/1
 */
@Extension(value = "json")
public final class JsonMessageConverter extends AbstractHttpMessageConverter {

    public JsonMessageConverter() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    public <T> T convert(InputStream inputStream, Class<T> respClass) {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
            return null;
        }
        String data = new String(bytes, StandardCharsets.UTF_8);
        return JSON.read(data, respClass);
    }

    @Override
    protected boolean isSupport(Class<?> respClass) {
        return true;
    }
}
