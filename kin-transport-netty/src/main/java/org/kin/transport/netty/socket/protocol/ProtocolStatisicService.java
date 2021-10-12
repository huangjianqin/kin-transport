package org.kin.transport.netty.socket.protocol;

import org.kin.framework.Closeable;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.counter.Counters;
import org.kin.framework.counter.Reporters;
import org.kin.framework.log.LoggerOprs;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 协议数据统计
 * 单例
 * important 要手动close
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ProtocolStatisicService implements Closeable, LoggerOprs {
    private static final ProtocolStatisicService INSTANCE = new ProtocolStatisicService();
    /** 请求数统计 */
    private static final String REQ_COUNTER = "reqCounter";
    /** 响应数统计 */
    private static final String RESP_COUNTER = "respCounter";
    /** 请求字节数统计 */
    private static final String REQ_BYTES_COUNTER = "reqBytesCounter";
    /** 响应字节数统计 */
    private static final String RESP_BYTES_COUNTER = "respBytesCounter";

    /** 定时log future */
    private final Future<?> future;

    static {
        JvmCloseCleaner.DEFAULT().add(INSTANCE);
    }

    public static ProtocolStatisicService instance() {
        return INSTANCE;
    }

    //-------------------------------------------------------------------------------------------------------

    /** 统计线程 */
    private final ExecutionContext executionContext = ExecutionContext.fix(2, "statisic",
            1, "statisic-schedule");

    private ProtocolStatisicService() {
        //一分钟打印一次
        future = executionContext.scheduleAtFixedRate(() -> {
            log().info(System.lineSeparator().concat(Reporters.report()));
        }, 1, 1, TimeUnit.MINUTES);
    }


    @Override
    public void close() {
        future.cancel(true);
        executionContext.shutdown();
    }

    //-------------------------------------------------------------------------------------------------------

    public void statisticReq(String uuid, long size) {
        Counters.increment(REQ_COUNTER, uuid);
        Counters.increment(REQ_BYTES_COUNTER, uuid, size);
    }

    public void statisticResp(String uuid, long size) {
        Counters.increment(RESP_COUNTER, uuid);
        Counters.increment(RESP_BYTES_COUNTER, uuid, size);
    }
}
