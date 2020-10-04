package org.kin.transport.netty.socket.protocol;

import java.lang.annotation.*;

/**
 * 协议中 长字符串 类型标识
 *
 * @author huangjianqin
 * @date 2020/10/4
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@Documented
public @interface BigString {
}
