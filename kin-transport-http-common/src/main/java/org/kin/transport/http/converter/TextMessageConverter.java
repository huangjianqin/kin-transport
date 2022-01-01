package org.kin.transport.http.converter;

import com.google.common.io.ByteStreams;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.Extension;
import org.kin.transport.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 转换成string
 *
 * @author huangjianqin
 * @date 2022/1/1
 */
@Extension(value = "text")
public final class TextMessageConverter extends AbstractHttpMessageConverter {

    public TextMessageConverter() {
        super(MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.TEXT_XML, MediaType.TEXT_MARKDOWN, MediaType.TEXT_EVENT_STREAM);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(InputStream inputStream, Class<T> respClass) {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            ExceptionUtils.throwExt(e);
            return null;
        }
        return (T) new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    protected boolean isSupport(Class<?> respClass) {
        return String.class == respClass;
    }
}

