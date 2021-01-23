package org.kin.transport.netty.http.server;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    /**
     * @return 请求参数名, 默认取方法参数名
     */
    String value() default "";

    /**
     * @return 是否必须存在, 如果检查到不存在, 则报错
     */
    boolean require() default true;
}
