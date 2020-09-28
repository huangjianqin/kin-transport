package org.kin.transport.netty.http.server;

import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2020/9/11
 */
public final class HttpServerConstants {
    /** session 移除时间(毫秒) */
    public static final long SESSION_EXPIRE_TIME = TimeUnit.MINUTES.toMillis(30);
    /** session id 长度 */
    public static final int SESSION_ID_LEN = 20;
    /** jvm route标识 */
    public static final String JVM_ROUTE = System.getProperty("jvmRoute");
    /** url 通配符 */
    public static final String URL_ALL_MATCH = "*";
}
