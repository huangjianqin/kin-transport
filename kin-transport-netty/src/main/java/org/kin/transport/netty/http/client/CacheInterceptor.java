package org.kin.transport.netty.http.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.jakewharton.disklrucache.DiskLruCache;
import org.kin.framework.log.LoggerOprs;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.MediaTypeWrapper;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * http cache 拦截器
 *
 * @author huangjianqin
 * @date 2020/9/21
 */
class CacheInterceptor implements Interceptor, LoggerOprs {
    static final CacheInterceptor INSTANCE = new CacheInterceptor();

    /**
     * 以kryo序列化存储
     * <p>
     * 解决多线程访问问题
     * 1.池化,
     * 2.ThreadLocal
     */
    private static final KryoPool KRYO_POOL = new KryoPool.Builder(() -> {
        final Kryo kryo = new Kryo();
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(
                new StdInstantiatorStrategy()));
        return kryo;
    }).softReferences().build();

    @Override
    public HttpResponse intercept(HttpInterceptorChain chain) throws IOException {
        HttpCall httpCall = chain.getCall();
        KinHttpClient kinHttpClient = httpCall.getHttpClient();
        HttpRequest httpRequest = httpCall.getRequest();
        CacheControl cacheControl = httpRequest.getCacheControl();

        if (Objects.nonNull(cacheControl) && !cacheControl.isNoCache() && !cacheControl.isNoStore()) {
            //使用缓存
            DiskLruCache cache = kinHttpClient.getCache();
            //key
            String key = CacheUtils.generateCacheKey(httpRequest);
            DiskLruCache.Snapshot snapshot = cache.get(key);
            if (Objects.nonNull(snapshot)) {
                //有缓存
                HttpResponseCache httpResponseCache = readCache(snapshot);
                if (httpResponseCache.isExpire()) {
                    log().debug("cache expired, key='{}'", key);
                    //缓存过期
                    if (!cacheControl.isMustRevalidate() && !httpResponseCache.isInvalid()) {
                        //允许使用过期数据
                        log().debug("cache expired, but still allow to use, key='{}'", key);
                        return httpResponseCache.getHttpResponse(httpRequest);
                    } else {
                        //缓存过期, 并且不可使用过去数据, 马上重新请求
                        log().debug("cache invalid, key='{}'", key);
                    }
                } else {
                    //缓存没有过期
                    log().debug("cache effect, key='{}'", key);
                    return httpResponseCache.getHttpResponse(httpRequest);
                }
            } else {
                //没有缓存
                log().debug("no cache, key='{}'", key);
            }
        }

        HttpResponse httpResponse = chain.proceed(httpRequest);

        //是否需要更新缓存
        if (Objects.nonNull(cacheControl) && !cacheControl.isNoCache() && !cacheControl.isNoStore()) {
            //使用缓存
            DiskLruCache cache = kinHttpClient.getCache();
            //key
            String key = CacheUtils.generateCacheKey(httpRequest);
            writeCache(cache, key, cacheControl, httpResponse);

            log().debug("cache update, key='{}'", key);
        }

        return httpResponse;
    }

    /**
     * 读取缓存
     */
    private HttpResponseCache readCache(DiskLruCache.Snapshot snapshot) {
        Kryo kryo = KRYO_POOL.borrow();
        try {
            Input input = new Input(snapshot.getInputStream(0));
            HttpResponseCache httpResponseCache = kryo.readObject(input, HttpResponseCache.class);
            input.close();
            snapshot.close();
            return httpResponseCache;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            KRYO_POOL.release(kryo);
        }
    }

    /**
     * 写 && 更新缓存
     */
    private void writeCache(DiskLruCache cache, String key, CacheControl cacheControl, HttpResponse httpResponse) {
        Kryo kryo = KRYO_POOL.borrow();
        try {
            HttpResponseCache httpResponseCache = new HttpResponseCache(httpResponse, cacheControl.getMaxAge(), cacheControl.getMaxStale());
            DiskLruCache.Editor edit = cache.edit(key);
            Output output = new Output(edit.newOutputStream(0));
            kryo.writeObject(output, httpResponseCache);
            output.close();
            edit.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            KRYO_POOL.release(kryo);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * http response缓存
     */
    private class HttpResponseCache implements Serializable {
        private static final long serialVersionUID = -3893822673322231181L;
        /** 响应内容 */
        private byte[] content;
        /** 类型 */
        private String mediaType;
        /** 编码 */
        private String charset;
        /** tips */
        private String message;
        /** response code */
        private int code;
        /** response headers */
        private HttpHeaders headers;
        /** 缓存过期时间, 毫秒(缓存过期后, 这份数据仍然可以使用(通过配置允许使用过期缓存数据)) */
        private long expireTime;
        /** 数据失效时间, 毫秒(过了这个时间, 这份缓存数据不会再使用) */
        private long invalidTime;

        public HttpResponseCache() {
        }

        public HttpResponseCache(HttpResponse httpResponse, int maxAge, int maxStale) {
            HttpResponseBody httpResponseBody = httpResponse.responseBody();
            this.content = httpResponseBody.bytes();
            MediaTypeWrapper mediaTypeWrapper = httpResponseBody.getMediaType();
            this.mediaType = mediaTypeWrapper.rawMediaType();
            this.charset = mediaTypeWrapper.rawCharset();
            this.message = httpResponse.message();
            this.code = httpResponse.code();
            this.headers = httpResponse.headers();

            if (maxAge <= 0) {
                maxAge = Integer.MAX_VALUE;
            }
            long now = System.currentTimeMillis();
            expireTime += (now + TimeUnit.SECONDS.toMillis(maxAge));

            if (maxStale <= 0) {
                maxStale = Integer.MAX_VALUE;
            }
            invalidTime += (now + TimeUnit.SECONDS.toMillis(maxStale));
        }

        /**
         * 转换成正式的http response
         */
        public HttpResponse getHttpResponse(HttpRequest httpRequest) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(content.length);
            byteBuffer.put(content);
            HttpResponseBody httpResponseBody = HttpResponseBody.of(byteBuffer, new MediaTypeWrapper(mediaType, charset));
            HttpResponse httpResponse = HttpResponse.of(httpResponseBody, message, code);
            httpResponse.headers(headers);
            httpResponse.setHttpRequest(httpRequest);
            return httpResponse;
        }

        /**
         * 缓存是否过期
         */
        public boolean isExpire() {
            return System.currentTimeMillis() >= expireTime;
        }

        /**
         * 缓存是否失效
         */
        public boolean isInvalid() {
            return System.currentTimeMillis() >= invalidTime;
        }

        //setter && getter
        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        public void setHeaders(HttpHeaders headers) {
            this.headers = headers;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public long getInvalidTime() {
            return invalidTime;
        }

        public void setInvalidTime(long invalidTime) {
            this.invalidTime = invalidTime;
        }
    }
}
