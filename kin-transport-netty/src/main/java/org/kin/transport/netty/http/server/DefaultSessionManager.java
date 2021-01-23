package org.kin.transport.netty.http.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.framework.utils.StringUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * session信息存储在内存中, 空闲一段时间后移除
 *
 * @author huangjianqin
 * @date 2020/9/11
 */
public final class DefaultSessionManager implements HttpSessionManager {
    /**
     * session 缓存, 读写空闲30min后移除
     */
    private final Cache<String, HttpSession> cache;
    /** session id 生成器 */
    private final SessionIdGenerator sessionIdGenerator;

    public DefaultSessionManager() {
        //session id长度为20
        //默认使用tomcat的session id生成方法
        this(HttpServerConstants.SESSION_EXPIRE_TIME, new DefaultSessionIdGenerator(HttpServerConstants.SESSION_ID_LEN));
    }

    public DefaultSessionManager(long sessionExpireTime) {
        //session id长度为20
        //默认使用tomcat的session id生成方法
        this(sessionExpireTime, new DefaultSessionIdGenerator(HttpServerConstants.SESSION_ID_LEN));
    }

    /**
     * @param sessionExpireTime session默认过期时间
     */
    public DefaultSessionManager(long sessionExpireTime, SessionIdGenerator sessionIdGenerator) {
        this.cache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(sessionExpireTime, TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<String, HttpSession> session) -> {
                    //设置无效标识
                    session.getValue().setValid(false);
                })
                .build();
        this.sessionIdGenerator = sessionIdGenerator;
    }


    @Override
    public HttpSession session(String sessionId) {
        if (StringUtils.isBlank(sessionId) || Objects.isNull(cache.getIfPresent(sessionId))) {
            //session 不存在 || 过期
            sessionId = sessionIdGenerator.generate(HttpServerConstants.JVM_ROUTE);
        }
        try {
            String finalSessionId = sessionId;
            return cache.get(sessionId, () -> new HttpSession(finalSessionId, this));
        } catch (ExecutionException e) {
            ExceptionUtils.throwExt(e);
        }
        throw new IllegalStateException("encounter unknown error");
    }

    @Override
    public void removeSession(String sessionId) {
        cache.invalidate(sessionId);
    }
}
