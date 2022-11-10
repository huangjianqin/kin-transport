package org.kin.transport.netty.http.server;

/**
 * http handler方法参数常量定义
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
interface ParamConstants {
    /**
     * 参考spring, {@link RequestParam}和{@link RequestHeader}默认值, 避开使用null
     *
     * @see RequestParam
     * @see RequestHeader
     */
    String DEFAULT_NONE = "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";
}
