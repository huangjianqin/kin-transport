package org.kin.transport.netty.http.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
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
public class DefaultSessionManager implements HttpSessionManager {
    /**
     * session 缓存, 读写空闲30min后移除
     */
    private final Cache<String, HttpSession> cache = CacheBuilder
            .newBuilder()
            .expireAfterAccess(HttpServerConstants.SESSION_EXPIRE_TIME, TimeUnit.MILLISECONDS)
            .removalListener((RemovalNotification<String, HttpSession> notification) -> {
                //设置无效标识
                notification.getValue().setValid(false);
            })
            .build();
    /** session id 生成器 */
    private final SessionIdGenerator sessionIdGenerator;

    public DefaultSessionManager() {
        //session id长度为20
        //默认使用tomcat的session id生成方法
        this(new DefaultSessionIdGenerator(HttpServerConstants.SESSION_ID_LEN));
    }

    public DefaultSessionManager(SessionIdGenerator sessionIdGenerator) {
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSession(String sessionId) {
        cache.invalidate(sessionId);
    }
}
