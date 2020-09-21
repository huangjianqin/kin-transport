package org.kin.transport.netty.http.client;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2020/9/21
 */
public class CacheControl {
    /** 不使用缓存，使用网络请求 */
    private boolean noCache;
    /** 不使用缓存也不存储缓存数据 */
    private boolean noStore;
    /** 一旦缓存过期，必须向服务器重新请求，不得使用过期内容 */
    private boolean mustRevalidate;
    /** 不得对响应进行转换或转变 todo */
    private boolean noTransform;
    /** 缓存的有效时间，超过该时间会重新请求数据, 秒 */
    private int maxAge;
    /** 超过缓存有效时间后，可继续使用旧缓存的时间，之后需要重新请求数据, 秒 */
    private int maxStale;

    //------------------------------------------------------------------------------------------------------------
    public static CacheControl create() {
        return new CacheControl();
    }

    //------------------------------------------------------------------------------------------------------------
    public CacheControl noCache() {
        noCache = true;
        return this;
    }

    public CacheControl noStore() {
        noStore = true;
        return this;
    }

    public CacheControl mustRevalidate() {
        mustRevalidate = true;
        return this;
    }

    public CacheControl noTransform() {
        noTransform = true;
        return this;
    }

    public CacheControl maxAge(long time, TimeUnit unit) {
        maxAge = (int) unit.toSeconds(time);
        return this;
    }

    public CacheControl maxStale(long time, TimeUnit unit) {
        maxStale = (int) unit.toSeconds(time);
        return this;
    }

    public String toHeaderStr() {
        StringJoiner sj = new StringJoiner(",");
        if (noCache) {
            sj.add("no-cache");
        }
        if (noStore) {
            sj.add("no-store");
        }
        if (mustRevalidate) {
            sj.add("must-revalidate");
        }
        if (noTransform) {
            sj.add("no-transform");
        }
        if (maxAge > 0) {
            sj.add("max-age=".concat(Integer.toString(maxAge)));
        }
        if (maxStale > 0) {
            sj.add("max-stale=".concat(Integer.toString(maxStale)));
        }

        return sj.toString();
    }

    //getter
    public boolean isNoCache() {
        return noCache;
    }

    public boolean isNoStore() {
        return noStore;
    }

    public boolean isMustRevalidate() {
        return mustRevalidate;
    }

    public boolean isNoTransform() {
        return noTransform;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public int getMaxStale() {
        return maxStale;
    }
}
