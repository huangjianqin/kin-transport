package org.kin.transport.netty.core.protocol;

import org.kin.transport.netty.core.DoNothingRateLimitCallback;
import org.kin.transport.netty.core.ProtocolRateLimitCallback;

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
     * 包处理间隔, 相当于网络层限流. 小于该间隔的, 一律抛弃不处理
     * 默认不抛弃
     */
    long rate() default -1;
    Class<? extends ProtocolRateLimitCallback> callback() default DoNothingRateLimitCallback.class;
}
