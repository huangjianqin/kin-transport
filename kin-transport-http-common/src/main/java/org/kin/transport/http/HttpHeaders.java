package org.kin.transport.http;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * http header封装, 推荐使用static来作为header name
 * 内置set方法都会将header name转成小写存储
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
public class HttpHeaders implements Serializable {
    private static final long serialVersionUID = 5792818876509538762L;

    //-------------------------------------------------------------http header定义-------------------------------------------------------------
    /**
     * The HTTP {@code Accept} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.2">Section 5.3.2 of RFC 7231</a>
     */
    public static final String ACCEPT = "accept";
    /**
     * The HTTP {@code Accept-Charset} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.3">Section 5.3.3 of RFC 7231</a>
     */
    public static final String ACCEPT_CHARSET = "accept-charset";
    /**
     * The HTTP {@code Accept-Encoding} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.4">Section 5.3.4 of RFC 7231</a>
     */
    public static final String ACCEPT_ENCODING = "accept-encoding";
    /**
     * The HTTP {@code Accept-Language} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.3.5">Section 5.3.5 of RFC 7231</a>
     */
    public static final String ACCEPT_LANGUAGE = "accept-language";
    /**
     * The HTTP {@code Accept-Patch} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc5789#section-3.1">Section 3.1 of RFC 5789</a>
     * @since 5.3.6
     */
    public static final String ACCEPT_PATCH = "accept-patch";
    /**
     * The HTTP {@code Accept-Ranges} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-2.3">Section 5.3.5 of RFC 7233</a>
     */
    public static final String ACCEPT_RANGES = "accept-ranges";
    /**
     * The CORS {@code Access-Control-Allow-Credentials} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "access-control-allow-credentials";
    /**
     * The CORS {@code Access-Control-Allow-Headers} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "access-control-allow-headers";
    /**
     * The CORS {@code Access-Control-Allow-Methods} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "access-control-allow-methods";
    /**
     * The CORS {@code Access-Control-Allow-Origin} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "access-control-allow-origin";
    /**
     * The CORS {@code Access-Control-Expose-Headers} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "access-control-expose-headers";
    /**
     * The CORS {@code Access-Control-Max-Age} response header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_MAX_AGE = "access-control-max-age";
    /**
     * The CORS {@code Access-Control-Request-Headers} request header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "access-control-request-headers";
    /**
     * The CORS {@code Access-Control-Request-Method} request header field name.
     *
     * @see <a href="https://www.w3.org/TR/cors/">CORS W3C recommendation</a>
     */
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "access-control-request-method";
    /**
     * The HTTP {@code Age} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.1">Section 5.1 of RFC 7234</a>
     */
    public static final String AGE = "age";
    /**
     * The HTTP {@code Allow} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.1">Section 7.4.1 of RFC 7231</a>
     */
    public static final String ALLOW = "allow";
    /**
     * The HTTP {@code Authorization} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.2">Section 4.2 of RFC 7235</a>
     */
    public static final String AUTHORIZATION = "authorization";
    /**
     * The HTTP {@code Cache-Control} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.2">Section 5.2 of RFC 7234</a>
     */
    public static final String CACHE_CONTROL = "cache-control";
    /**
     * The HTTP {@code Connection} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.1">Section 6.1 of RFC 7230</a>
     */
    public static final String CONNECTION = "connection";
    /**
     * The HTTP {@code Content-Encoding} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">Section 3.1.2.2 of RFC 7231</a>
     */
    public static final String CONTENT_ENCODING = "content-encoding";
    /**
     * The HTTP {@code Content-Disposition} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6266">RFC 6266</a>
     */
    public static final String CONTENT_DISPOSITION = "content-disposition";
    /**
     * The HTTP {@code Content-Language} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.3.2">Section 3.1.3.2 of RFC 7231</a>
     */
    public static final String CONTENT_LANGUAGE = "content-language";
    /**
     * The HTTP {@code Content-Length} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2">Section 3.3.2 of RFC 7230</a>
     */
    public static final String CONTENT_LENGTH = "content-length";
    /**
     * The HTTP {@code Content-Location} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.4.2">Section 3.1.4.2 of RFC 7231</a>
     */
    public static final String CONTENT_LOCATION = "content-location";
    /**
     * The HTTP {@code Content-Range} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-4.2">Section 4.2 of RFC 7233</a>
     */
    public static final String CONTENT_RANGE = "content-range";
    /**
     * The HTTP {@code Content-Type} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">Section 3.1.1.5 of RFC 7231</a>
     */
    public static final String CONTENT_TYPE = "content-type";
    /**
     * The HTTP {@code Cookie} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.3.4">Section 4.3.4 of RFC 2109</a>
     */
    public static final String COOKIE = "cookie";
    /**
     * The HTTP {@code Date} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.2">Section 7.1.1.2 of RFC 7231</a>
     */
    public static final String DATE = "date";
    /**
     * The HTTP {@code ETag} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
     */
    public static final String ETAG = "etag";
    /**
     * The HTTP {@code Expect} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.1">Section 5.1.1 of RFC 7231</a>
     */
    public static final String EXPECT = "expect";
    /**
     * The HTTP {@code Expires} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.3">Section 5.3 of RFC 7234</a>
     */
    public static final String EXPIRES = "expires";
    /**
     * The HTTP {@code From} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.1">Section 5.5.1 of RFC 7231</a>
     */
    public static final String FROM = "from";
    /**
     * The HTTP {@code Host} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.4">Section 5.4 of RFC 7230</a>
     */
    public static final String HOST = "host";
    /**
     * The HTTP {@code If-Match} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.1">Section 3.1 of RFC 7232</a>
     */
    public static final String IF_MATCH = "if-match";
    /**
     * The HTTP {@code If-Modified-Since} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.3">Section 3.3 of RFC 7232</a>
     */
    public static final String IF_MODIFIED_SINCE = "if-modified-since";
    /**
     * The HTTP {@code If-None-Match} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.2">Section 3.2 of RFC 7232</a>
     */
    public static final String IF_NONE_MATCH = "if-none-match";
    /**
     * The HTTP {@code If-Range} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.2">Section 3.2 of RFC 7233</a>
     */
    public static final String IF_RANGE = "if-range";
    /**
     * The HTTP {@code If-Unmodified-Since} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-3.4">Section 3.4 of RFC 7232</a>
     */
    public static final String IF_UNMODIFIED_SINCE = "if-unmodified-since";
    /**
     * The HTTP {@code Last-Modified} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.2">Section 2.2 of RFC 7232</a>
     */
    public static final String LAST_MODIFIED = "last-modified";
    /**
     * The HTTP {@code Link} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc5988">RFC 5988</a>
     */
    public static final String LINK = "link";
    /**
     * The HTTP {@code Location} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.2">Section 7.1.2 of RFC 7231</a>
     */
    public static final String LOCATION = "location";
    /**
     * The HTTP {@code Max-Forwards} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.1.2">Section 5.1.2 of RFC 7231</a>
     */
    public static final String MAX_FORWARDS = "max-forwards";
    /**
     * The HTTP {@code Origin} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6454">RFC 6454</a>
     */
    public static final String ORIGIN = "origin";
    /**
     * The HTTP {@code Pragma} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.4">Section 5.4 of RFC 7234</a>
     */
    public static final String PRAGMA = "pragma";
    /**
     * The HTTP {@code Proxy-Authenticate} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.3">Section 4.3 of RFC 7235</a>
     */
    public static final String PROXY_AUTHENTICATE = "proxy-authenticate";
    /**
     * The HTTP {@code Proxy-Authorization} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.4">Section 4.4 of RFC 7235</a>
     */
    public static final String PROXY_AUTHORIZATION = "proxy-authorization";
    /**
     * The HTTP {@code Range} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7233#section-3.1">Section 3.1 of RFC 7233</a>
     */
    public static final String RANGE = "range";
    /**
     * The HTTP {@code Referer} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.2">Section 5.5.2 of RFC 7231</a>
     */
    public static final String REFERER = "referer";
    /**
     * The HTTP {@code Retry-After} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.3">Section 7.1.3 of RFC 7231</a>
     */
    public static final String RETRY_AFTER = "retry-after";
    /**
     * The HTTP {@code Server} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.4.2">Section 7.4.2 of RFC 7231</a>
     */
    public static final String SERVER = "server";
    /**
     * The HTTP {@code Set-Cookie} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2109#section-4.2.2">Section 4.2.2 of RFC 2109</a>
     */
    public static final String SET_COOKIE = "set-cookie";
    /**
     * The HTTP {@code Set-Cookie2} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2965">RFC 2965</a>
     */
    public static final String SET_COOKIE2 = "set-cookie2";
    /**
     * The HTTP {@code TE} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.3">Section 4.3 of RFC 7230</a>
     */
    public static final String TE = "te";
    /**
     * The HTTP {@code Trailer} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-4.4">Section 4.4 of RFC 7230</a>
     */
    public static final String TRAILER = "trailer";
    /**
     * The HTTP {@code Transfer-Encoding} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-3.3.1">Section 3.3.1 of RFC 7230</a>
     */
    public static final String TRANSFER_ENCODING = "transfer-encoding";
    /**
     * The HTTP {@code Upgrade} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-6.7">Section 6.7 of RFC 7230</a>
     */
    public static final String UPGRADE = "upgrade";
    /**
     * The HTTP {@code User-Agent} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-5.5.3">Section 5.5.3 of RFC 7231</a>
     */
    public static final String USER_AGENT = "user-agent";
    /**
     * The HTTP {@code Vary} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.4">Section 7.1.4 of RFC 7231</a>
     */
    public static final String VARY = "vary";
    /**
     * The HTTP {@code Via} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7230#section-5.7.1">Section 5.7.1 of RFC 7230</a>
     */
    public static final String VIA = "via";
    /**
     * The HTTP {@code Warning} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7234#section-5.5">Section 5.5 of RFC 7234</a>
     */
    public static final String WARNING = "warning";
    /**
     * The HTTP {@code WWW-Authenticate} header field name.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7235#section-4.1">Section 4.1 of RFC 7235</a>
     */
    public static final String WWW_AUTHENTICATE = "www-authenticate";

    //-------------------------------------------------------------custom-------------------------------------------------------------
    /** 默认编码 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** empty */
    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final HttpHeaders EMPTY = new ReadOnlyHttpHeaders();

    /**
     * format HTTP header values
     */
    public static String formatHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .map(entry -> {
                    List<String> values = (List<String>) entry.getValue();
                    return entry.getKey() + ":" + (values.size() == 1 ?
                            "\"" + values.get(0) + "\"" :
                            values.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /** 底层存储http header key-value的map */
    private final ListMultimap<String, String> map = MultimapBuilder.hashKeys().linkedListValues().build();

    public HttpHeaders() {
    }

    public HttpHeaders(Multimap<String, String> keyValues) {
        putAll(keyValues);
    }

    public HttpHeaders(Map<String, List<String>> keyValues) {
        putAll(keyValues);
    }

    public HttpHeaders(HttpHeaders headers) {
        putAll(headers.map);
    }

    /**
     * 获取字符集
     */
    public String getCharset() {
        //先尝试从Accept-Charset获取
        String acceptCharset = getFirst(HttpHeaders.ACCEPT_CHARSET);
        if (StringUtils.isBlank(acceptCharset)) {
            //找不到, 则尝试从Content-Type获取
            String contentType = getFirst(HttpHeaders.CONTENT_TYPE);
            //如果都找不到, 默认"UTF-8"
            acceptCharset = StringUtils.isNotBlank(contentType) ? extractCharsetFromContentType(contentType) : DEFAULT_CHARSET;
        }
        return acceptCharset;
    }

    /**
     * 从Content-Type获取字符集
     */
    private String extractCharsetFromContentType(String contentType) {
        String[] values = contentType.split(";");
        String charset = DEFAULT_CHARSET;
        if (values.length == 0) {
            return charset;
        }
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return charset;
    }

    /**
     * set {@link #CONTENT_LENGTH} value
     */
    public void setContentLength(long contentLength) {
        put(CONTENT_LENGTH, Long.toString(contentLength));
    }

    /**
     * 获取{@link #CONTENT_TYPE} value
     */
    @Nullable
    public String getContentType() {
        return getFirst(CONTENT_TYPE);
    }
    //-------------------------------------------------------------http header add,get,remove operation-------------------------------------------------------------

    /**
     * put http header和value
     */
    public void put(String key, @Nullable String value) {
        map.put(key.toLowerCase(), value);
    }

    /**
     * put http header和values
     */
    public void putAll(String key, List<String> values) {
        map.putAll(key.toLowerCase(), values);
    }

    /**
     * put http header和values
     */
    public void putAll(Map<String, List<String>> keyValues) {
        for (Map.Entry<String, List<String>> entry : keyValues.entrySet()) {
            map.putAll(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * put http header和values
     */
    public void putAll(Multimap<String, String> keyValues) {
        for (Map.Entry<String, Collection<String>> entry : keyValues.asMap().entrySet()) {
            map.putAll(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * set http header和values
     */
    public List<String> set(String key, List<String> values) {
        return map.replaceValues(key.toLowerCase(), values);
    }

    /**
     * set all http header和values
     */
    public void setAll(Map<String, List<String>> keyValues) {
        for (Map.Entry<? extends String, ? extends List<String>> entry : keyValues.entrySet()) {
            keyValues.replace(entry.getKey().toLowerCase(), entry.getValue());
        }
    }

    /**
     * 获取header数量
     */
    public int size() {
        return map.size();
    }

    /**
     * header是否为空
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 是否包含指定header
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * 是否包含指定header value
     */
    public boolean containsValue(String value) {
        return map.containsValue(value);
    }

    /**
     * 获取header values
     */
    @Nullable
    public List<String> get(String key) {
        return map.get(key);
    }

    /**
     * 获取header values的第一个value
     */
    @Nullable
    public String getFirst(String key) {
        List<String> values = map.get(key);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    /**
     * 移除指定http header
     */
    public List<String> remove(String key) {
        return map.removeAll(key);
    }

    /**
     * 清空所有http header和values
     */
    public void clear() {
        map.clear();
    }

    /**
     * 获取所有http header
     */
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * 获取所有http header values
     */
    public Collection<Collection<String>> values() {
        return map.asMap().values();
    }

    /**
     * 获取所有http header和其values
     */
    public Set<Map.Entry<String, Collection<String>>> entrySet() {
        return map.asMap().entrySet();
    }

    /**
     * 转换成{@code Map<String, String>}
     */
    public Map<String, String> toSingleValueMap() {
        // TODO: 2022/1/1 看看逻辑是否需要修改
        Map<String, String> ret = new HashMap<>(map.size());
        for (String key : map.keySet()) {
            List<String> values = get(key);
            if (CollectionUtils.isEmpty(values)) {
                continue;
            }

            ret.put(key, values.get(0));
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpHeaders)) {
            return false;
        }
        HttpHeaders that = (HttpHeaders) o;
        return Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return formatHeaders(this);
    }
}
