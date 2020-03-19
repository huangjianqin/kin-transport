package org.kin.transport.netty.core.protocol;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Protocol {
    int id();

    /**
     * 包处理间隔, 相当于网络层控流. 小于该间隔的, 一律抛弃不处理
     * 默认不抛弃
     */
    int rate() default -1;
}
