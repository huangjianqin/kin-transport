package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.io.Serializable;

/**
 * @author huangjianqin
 * @date 2020/9/10
 */
public final class Cookie implements Serializable, Cloneable {
    /** cookie名 */
    private final String name;
    /** cookie值 */
    private final String value;
    /** cookie域 */
    private String domain;
    /** cookie存活时间 */
    private long maxAge = -1;
    /** cookie path */
    private String path;
    /** 是否加密 */
    private boolean secure;
    /** Not in cookie specs, but supported by browsers */
    private boolean httpOnly;

    private Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    io.netty.handler.codec.http.cookie.Cookie toNettyCookie() {
        DefaultCookie nettyCookie = new DefaultCookie(name, value);
        nettyCookie.setDomain(domain);
        nettyCookie.setMaxAge(maxAge);
        nettyCookie.setPath(path);
        nettyCookie.setSecure(secure);
        nettyCookie.setHttpOnly(httpOnly);
        return nettyCookie;
    }

    //------------------------------------------------------------------------------------------------------------

    public static Cookie of(String name, String value) {
        return new Cookie(name, value);
    }

    public static Cookie of(io.netty.handler.codec.http.cookie.Cookie nettyCookie) {
        Cookie cookie = new Cookie(nettyCookie.name(), nettyCookie.value());
        cookie.domain = nettyCookie.domain();
        cookie.maxAge = nettyCookie.maxAge();
        cookie.path = nettyCookie.path();
        cookie.secure = nettyCookie.isSecure();
        cookie.httpOnly = nettyCookie.isHttpOnly();
        return cookie;
    }

    //------------------------------------------------------------------------------------------------------------


    @Override
    public Cookie clone() throws CloneNotSupportedException {
        Cookie copy = new Cookie(name, value);
        copy.domain = domain;
        copy.maxAge = maxAge;
        copy.path = path;
        copy.secure = secure;
        copy.httpOnly = httpOnly;
        return copy;
    }

    //setter && getter
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
}
