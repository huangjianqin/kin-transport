package org.kin.transport.netty.http.server;

import java.lang.annotation.*;

/**
 * 从request header取值
 *
 * @author huangjianqin
 * @date 2022/11/9
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeader {
    /**
     * @return 请求参数名, 默认取方法参数名
     */
    String value() default "";

    /**
     * @return 是否必须存在, 如果检查到不存在, 则报错
     */
    boolean require() default true;

    /**
     * 不存在时, 参数默认值; 设置了默认值, 则{@link #require()}=false
     *
     * @return 默认值
     */
    String defaultValue() default ParamConstants.DEFAULT_NONE;
}
