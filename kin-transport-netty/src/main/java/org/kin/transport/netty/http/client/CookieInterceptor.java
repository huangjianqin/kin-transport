package org.kin.transport.netty.http.client;

import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.StringUtils;
import org.kin.framework.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.COOKIE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;

/**
 * cookie cache interceptor
 *
 * @author huangjianqin
 * @date 2020/9/29
 */
class CookieInterceptor implements Interceptor, LoggerOprs {
    /** 单例 */
    public static final CookieInterceptor INSTANCE = new CookieInterceptor();

    /** cookie cache */
    private final Map<String, Map<String, CookieInfo>> cookieCache = new ConcurrentHashMap<>();
    /** cookie encoder */
    private final ClientCookieEncoder cookieEncoder = ClientCookieEncoder.STRICT;
    /** cookie decoder */
    private final ClientCookieDecoder cookieDecoder = ClientCookieDecoder.STRICT;

    /**
     * 移除过期cookie
     */
    private Map<String, CookieInfo> filterCookie(String cacheKey, Map<String, CookieInfo> cookies) {
        //age过滤
        Map<String, CookieInfo> filteredCookies = cookies.entrySet()
                .stream()
                .filter(e -> !e.getValue().isExpire())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        cookieCache.put(cacheKey, filteredCookies);
        return filteredCookies;
    }

    /**
     * 设置cookies
     */
    private void setCookies(HttpRequest httpRequest) {
        String cacheKey = CacheUtils.generateCacheKey(httpRequest);
        Map<String, CookieInfo> cookies = cookieCache.get(cacheKey);
        if (CollectionUtils.isNonEmpty(cookies)) {
            cookies = filterCookie(cacheKey, cookies);
            httpRequest.header(COOKIE.toString(),
                    cookieEncoder.encode(cookies.values().stream().map(CookieInfo::getCookie).collect(Collectors.toList())));
        }
    }

    /**
     * 设置cookies
     */
    private void updateCookies(HttpRequest httpRequest, HttpResponse httpResponse) {
        String setCookie = httpResponse.headers().get(SET_COOKIE.toString());
        if (StringUtils.isNotBlank(setCookie)) {
            Cookie cookie = cookieDecoder.decode(setCookie);
            String cacheKey = CacheUtils.generateCacheKey(httpRequest);
            Map<String, CookieInfo> cookies = new HashMap<>(cookieCache.get(cacheKey));
            cookies.put(cookie.name(), new CookieInfo(cookie));
            cookieCache.put(cacheKey, cookies);
        }
    }

    @Override
    public HttpResponse intercept(HttpInterceptorChain chain) {
        HttpRequest httpRequest = chain.getCall().getRequest();

        //set cookie
        setCookies(httpRequest);

        HttpResponse httpResponse = chain.proceed(httpRequest);

        //update cookie
        updateCookies(httpRequest, httpResponse);

        return httpResponse;
    }

    //----------------------------------------------------------------------------------------------------------------

    /**
     * http client cookie信息
     */
    private class CookieInfo {
        /** cookie元数据 */
        private final Cookie cookie;
        /** cookie更新时间(时间戳) */
        private final int updateTime;

        public CookieInfo(Cookie cookie) {
            this.cookie = cookie;
            updateTime = TimeUtils.timestamp();
        }

        /**
         * @return 是否过期
         */
        public boolean isExpire() {
            return updateTime + cookie.maxAge() > TimeUtils.timestamp();
        }

        public Cookie getCookie() {
            return cookie;
        }

        public int getUpdateTime() {
            return updateTime;
        }
    }
}
