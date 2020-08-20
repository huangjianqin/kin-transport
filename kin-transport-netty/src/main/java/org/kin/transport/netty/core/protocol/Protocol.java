package org.kin.transport.netty.core.protocol;

import java.lang.annotation.*;

/**
 * 协议标识注解
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Protocol {
    /**
     * 协议号
     */
    int id();

    /**
     * 包处理间隔, 相当于网络层控流. 小于该间隔的, 一律抛弃不处理
     * 默认不抛弃
     */
    int rate() default -1;
}
