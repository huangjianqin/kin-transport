package org.kin.transport.netty.socket.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by huangjianqin on 2019/6/4.
 */
class ProtocolStatistic {
    /**
     * serviceName+method || protocolId
     */
    private String uuid;
    private AtomicLong totalSize;
    private AtomicLong count;

    ProtocolStatistic(String uuid) {
        this.uuid = uuid;
        totalSize = new AtomicLong(0);
        count = new AtomicLong(0);
    }

    void incr(long size) {
        totalSize.addAndGet(size);
        count.incrementAndGet();
    }

    @Override
    public String toString() {
        return "uuid: " + uuid + ">>>" + totalSize + "-" + count;
    }
}
