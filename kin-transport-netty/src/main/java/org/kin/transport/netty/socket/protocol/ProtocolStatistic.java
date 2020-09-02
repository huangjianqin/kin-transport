package org.kin.transport.netty.socket.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 协议数据统计
 * Created by huangjianqin on 2019/6/4.
 */
class ProtocolStatistic {
    /** protocolId */
    private String uuid;
    private AtomicLong totalSize;
    private AtomicLong count;

    ProtocolStatistic(String uuid) {
        this.uuid = uuid;
        totalSize = new AtomicLong(0);
        count = new AtomicLong(0);
    }

    /**
     * 统计协议数据量以及数量
     */
    void incr(long size) {
        totalSize.addAndGet(size);
        count.incrementAndGet();
    }

    @Override
    public String toString() {
        return "uuid: " + uuid + ">>>" + totalSize + "-" + count;
    }
}
