package org.kin.transport.netty.http.server;

import java.lang.annotation.*;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostMapping {
    /**
     * @return url
     */
    String value();
}
