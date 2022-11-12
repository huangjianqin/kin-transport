package org.kin.transport.netty.http.server;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    /**
     * @return url
     */
    String value();

    /**
     * @return {@link RequestMethod} array
     */
    RequestMethod[] method() default {};
}
