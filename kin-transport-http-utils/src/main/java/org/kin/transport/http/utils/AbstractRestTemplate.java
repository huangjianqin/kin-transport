package org.kin.transport.http.utils;

import org.kin.framework.utils.ExtensionLoader;
import org.kin.transport.http.HttpResponse;
import org.kin.transport.http.converter.MessageConverter;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * {@link RestTemplate}抽象, 包含已注册的所有{@link MessageConverter}
 *
 * @author huangjianqin
 * @date 2022/1/1
 */
public abstract class AbstractRestTemplate implements RestTemplate {
    /** loaded http message converter集合 */
    protected static final List<MessageConverter> CONVERTERS;

    static {
        CONVERTERS = Collections.unmodifiableList(ExtensionLoader.getExtensions(MessageConverter.class));
    }

    /**
     * 将http received data转换成java pojo
     *
     * @see MessageConverter
     */
    protected <T> T convert(HttpResponse httpResponse, InputStream inputStream, Class<T> respClass) {
        //遍历所有已加载的converter
        for (MessageConverter extension : CONVERTERS) {
            if (!extension.canConvert(httpResponse, respClass)) {
                //不满足convert条件
                continue;
            }
            //convert
            return extension.convert(inputStream, respClass);
        }

        return null;
    }
}
