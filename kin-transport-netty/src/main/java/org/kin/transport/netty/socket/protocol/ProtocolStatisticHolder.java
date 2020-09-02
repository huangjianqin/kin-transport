package org.kin.transport.netty.socket.protocol;


import org.kin.framework.concurrent.partition.EfficientHashPartitioner;
import org.kin.framework.concurrent.partition.Partitioner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 协议数据统计持有者
 *
 * @author huangjianqin
 * @date 2019/6/4
 */
public class ProtocolStatisticHolder {
    /** 分段锁数量 */
    private static final byte LOCK_NUM = 5;

    /** 持有的channel数 */
    private final AtomicLong ref = new AtomicLong(0);
    /** 协议数据统计 */
    private final Map<String, ProtocolStatistic> statisticMap = new ConcurrentHashMap<>();
    /** 分段锁 */
    private final Object[] locks = new Object[LOCK_NUM];
    /** 分段算法 */
    private final Partitioner<String> partitioner = EfficientHashPartitioner.INSTANCE;

    ProtocolStatisticHolder() {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }
    }

    /**
     * 根据uuid 获取的协议数据统计
     */
    ProtocolStatistic getstatistic(String uuid) {
        if (!statisticMap.containsKey(uuid)) {
            Object lock = locks[partitioner.toPartition(uuid, LOCK_NUM)];
            synchronized (lock) {
                if (!statisticMap.containsKey(uuid)) {
                    statisticMap.put(uuid, new ProtocolStatistic(uuid));
                }
            }
        }

        return statisticMap.get(uuid);
    }

    /**
     * 打印统计数据
     */
    String logContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        for (ProtocolStatistic statistic : statisticMap.values()) {
            sb.append(statistic.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * 被channel持有
     */
    void reference() {
        ref.incrementAndGet();
    }

    /**
     * channel释放持有
     */
    void release() {
        ref.decrementAndGet();
    }

    /**
     * @return channel持有数
     */
    long getRef() {
        return ref.get();
    }
}
