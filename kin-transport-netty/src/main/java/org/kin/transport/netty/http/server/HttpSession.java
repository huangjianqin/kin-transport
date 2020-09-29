package org.kin.transport.netty.http.server;

import org.kin.framework.utils.StringUtils;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * http server session
 *
 * @author huangjianqin
 * @date 2020/8/25
 */
public final class HttpSession {
    /** cookies 中保存的session id key */
    public static final String SESSION_ID = "_kinId";

    /** session id */
    private final String sessionId;
    /** 从属的session manager */
    private final HttpSessionManager sessionManager;

    /** 属性 */
    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    /** 创建时间 */
    private final long createTime;
    /** session是否有效 */
    private volatile boolean valid = true;

    public HttpSession(String sessionId, HttpSessionManager sessionManager) {
        this.sessionId = sessionId;
        this.sessionManager = sessionManager;
        this.createTime = System.currentTimeMillis();
    }

    /**
     * 获取属性
     */
    public Object getAttribute(String name) {
        if (!isValid()) {
            throw new IllegalStateException("session invalid");
        }

        if (StringUtils.isBlank(name)) {
            return null;
        }

        return attributes.get(name);
    }

    /**
     * 设置属性
     */
    public void putAttribute(String name, Object value) {
        if (!isValid()) {
            throw new IllegalStateException("session invalid");
        }

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is blank");
        }

        if (Objects.isNull(value)) {
            removeAttribute(name);
            return;
        }

        attributes.put(name, value);
    }

    /**
     * 移除属性
     */
    public void removeAttribute(String name) {
        if (!isValid()) {
            throw new IllegalStateException("session invalid");
        }

        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is blank");
        }
        attributes.remove(name);
    }

    /**
     * 主动触发session过期
     */
    public void expire() {
        if (!isValid()) {
            throw new IllegalStateException("session has invalid");
        }

        sessionManager.removeSession(sessionId);
    }

    //setter && getter
    public String getSessionId() {
        return sessionId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
