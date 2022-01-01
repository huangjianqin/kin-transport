package org.kin.transport.http.converter;

import org.kin.framework.utils.SPI;
import org.kin.transport.http.HttpResponse;

import java.io.InputStream;

/**
 * 将http received data转换成java pojo
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
@SPI
public interface MessageConverter {
    /** 检查指定received data允许convert */
    boolean canConvert(HttpResponse response, Class<?> respClass);

    /**
     * 将http received data转换为java pojo
     */
    <T> T convert(InputStream inputStream, Class<T> respClass);
}
