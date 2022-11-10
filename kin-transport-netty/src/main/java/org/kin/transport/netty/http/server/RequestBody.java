package org.kin.transport.netty.http.server;

import java.lang.annotation.*;

/**
 * 从request body取参数
 *
 * @author huangjianqin
 * @date 2022/11/9
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    /**
     * @return 是否必须存在, 如果检查到不存在, 则报错
     */
    boolean require() default true;
}
