package org.kin.transport.netty.http.client;

import com.google.common.base.Preconditions;
import com.jakewharton.disklrucache.DiskLruCache;
import io.netty.channel.ChannelOption;
import org.kin.framework.Closeable;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 对外的http client api
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
public final class KinHttpClient implements Closeable {
    /** http client池 */
    private final HttpClientPool pool;
    /** interceptors */
    private List<Interceptor> interceptors = new ArrayList<>();
    /** 请求重试次数, 默认不断重试 */
    private int retryTimes = -1;
    /** 连接超时,毫秒 */
    private long connectTimeout;
    /** http call超时,毫秒 */
    private long callTimeout;
    /** 读超时,毫秒 */
    private long readTimeout;
    /** 写超时,毫秒 */
    private long writeTimeout;
    /** 缓存目录 */
    private DiskLruCache cache;

    private KinHttpClient() {
        HttpClientTransportOption transportOption = new HttpClientTransportOption()
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .channelOption(ChannelOption.SO_KEEPALIVE, true);
        pool = new HttpClientPool(transportOption);
    }

    //builder
    public static KinHttpClient create() {
        return new KinHttpClient();
    }

    public KinHttpClient retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public KinHttpClient connectTimeout(long connectTimeout, TimeUnit unit) {
        this.connectTimeout = unit.toMillis(connectTimeout);
        return this;
    }

    public KinHttpClient callTimeout(long callTimeout, TimeUnit unit) {
        this.callTimeout = unit.toMillis(callTimeout);
        return this;
    }

    public KinHttpClient readTimeout(long readTimeout, TimeUnit unit) {
        this.readTimeout = unit.toMillis(readTimeout);
        return this;
    }

    public KinHttpClient writeTimeout(long writeTimeout, TimeUnit unit) {
        this.writeTimeout = unit.toMillis(writeTimeout);
        return this;
    }

    public KinHttpClient addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    public KinHttpClient addInterceptors(Collection<Interceptor> interceptors) {
        this.interceptors.addAll(interceptors);
        return this;
    }

    public KinHttpClient removeInterceptor(Interceptor interceptor) {
        interceptors.remove(interceptor);
        return this;
    }

    public KinHttpClient removeInterceptors(Collection<Interceptor> interceptors) {
        this.interceptors.removeAll(interceptors);
        return this;
    }

    public KinHttpClient cacheDir(String dirPath, int maxSize) {
        return cacheDir(new File(dirPath), maxSize);
    }

    public KinHttpClient cacheDir(File cacheDir, int maxSize) {
        Preconditions.checkArgument(Objects.isNull(cache), "cache has been set");
        Preconditions.checkArgument(Objects.nonNull(cacheDir) && cacheDir.isDirectory(),
                "cache dir path must be a directory");
        //1个app版本
        //1个key对应1个value
        try {
            cache = DiskLruCache.open(cacheDir, 1, 1, maxSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    //--------------------------------------------------------------------------------------------------------------

    /**
     * 构建http call
     */
    public HttpCall newCall(HttpRequest request) {
        return new HttpCall(this, request);
    }

    //--------------------------------------------------------------------------------------------------------------

    /**
     * 从http client池获取client
     */
    HttpClient client(InetSocketAddress address) {
        return pool.client(address);
    }

    /**
     * 将client归还到http client池
     */
    void clientBack(InetSocketAddress address, HttpClient client) {
        pool.clientBack(address, client);
    }

    @Override
    public void close() {
        if (Objects.nonNull(cache)) {
            try {
                cache.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //--------------------------------------------------------------------------------------------------------------
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public long getCallTimeout() {
        return callTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public DiskLruCache getCache() {
        return cache;
    }
}
