package org.kin.transport.netty.http.client;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * http client连接池
 *
 * @author huangjianqin
 * @date 2020/9/4
 */
final class HttpClientPool {
    private final HttpClientTransportOption<HttpEntity> transportOption;
    private final Map<InetSocketAddress, GenericObjectPool<HttpClient>> pools = new ConcurrentHashMap<>();

    public HttpClientPool(HttpClientTransportOption<HttpEntity> transportOption) {
        this.transportOption = transportOption;
    }

    /**
     * 获取http client
     */
    public HttpClient client(InetSocketAddress address) {
        try {
            synchronized (this) {
                if (!pools.containsKey(address)) {
                    pools.put(address, createPool(address));
                }
                return pools.get(address).borrowObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 归还http client
     */
    public void clientBack(InetSocketAddress address, HttpClient httpClient) {
        pools.get(address).returnObject(httpClient);
    }

    /**
     * 创建连接池
     */
    private GenericObjectPool<HttpClient> createPool(InetSocketAddress address) {
        //池化对象创建工厂
        HttpClientFactory httpClientFactory = new HttpClientFactory(address);
        //池配置
        GenericObjectPoolConfig<HttpClient> poolConfig = new GenericObjectPoolConfig<>();
        //最大空闲数
        poolConfig.setMaxIdle(5);
        //池最大对象数
        poolConfig.setMaxTotal(50);
        //获取池对象等待时间, 毫秒
        poolConfig.setMaxWaitMillis(TimeUnit.SECONDS.toMillis(3));
        //空闲等待时间, 毫秒
        poolConfig.setMinEvictableIdleTimeMillis(TimeUnit.MINUTES.toMillis(5));
        poolConfig.setTestOnCreate(true);
        poolConfig.setTestOnBorrow(true);
        //new
        return new GenericObjectPool<>(httpClientFactory, poolConfig);
    }

    //------------------------------------------------------------------------------------------------------------
    /**
     * http client工厂
     */
    private class HttpClientFactory extends BasePooledObjectFactory<HttpClient> {
        /** 远程服务器地址 */
        private final InetSocketAddress address;

        private HttpClientFactory(InetSocketAddress address) {
            this.address = address;
        }

        @Override
        public HttpClient create() throws Exception {
            return (HttpClient) transportOption.build(address);
        }

        @Override
        public PooledObject<HttpClient> wrap(HttpClient httpClient) {
            return new DefaultPooledObject<>(httpClient);
        }

        @Override
        public void destroyObject(PooledObject<HttpClient> po) {
            po.getObject().close();
        }

        @Override
        public boolean validateObject(PooledObject<HttpClient> po) {
            return po.getObject().isActive();
        }
    }
}
