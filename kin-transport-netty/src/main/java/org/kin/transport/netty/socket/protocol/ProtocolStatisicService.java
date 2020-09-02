package org.kin.transport.netty.socket.protocol;

import org.kin.framework.Closeable;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.concurrent.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 协议数据统计
 * 单例
 * important 要手动close
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public class ProtocolStatisicService implements Closeable {
    private static final Logger reqStatisticLog = LoggerFactory.getLogger("reqStatistic");
    private static final Logger respStatisticLog = LoggerFactory.getLogger("respStatistic");
    private static final ProtocolStatisicService INSTANCE = new ProtocolStatisicService();

    static {
        JvmCloseCleaner.DEFAULT().add(INSTANCE);
    }

    /**
     *
     */
    private ProtocolStatisticHolder reqHolder = new ProtocolStatisticHolder();
    /** */
    private ProtocolStatisticHolder respHolder = new ProtocolStatisticHolder();
    /** 统计线程 */
    private ExecutionContext executionContext = ExecutionContext.fix(2, "inoutbound-statisic",
            1, "inoutbound-statisic-schedule");

    private ProtocolStatisicService() {
        //一分钟打印一次
        executionContext.scheduleAtFixedRate(() -> {
            logReqStatistic();
            logRespStatistic();
        }, 1, 1, TimeUnit.MINUTES);
    }

    public static ProtocolStatisicService instance() {
        return INSTANCE;
    }

    @Override
    public void close() {
        executionContext.shutdown();
    }

    //-------------------------------------------------------------------------------------------------------

    private void logReqStatistic() {
        ProtocolStatisticHolder origin = reqHolder;
        reqHolder = new ProtocolStatisticHolder();
        logReqStatistic0(origin);
    }

    private void logReqStatistic0(ProtocolStatisticHolder origin) {
        if (origin != null) {
            if (origin.getRef() > 0) {
                executionContext.schedule(() -> logReqStatistic0(origin), 50, TimeUnit.MILLISECONDS);
                return;
            }
            logReqStatistic1(origin);
        }
    }

    private void logReqStatistic1(ProtocolStatisticHolder origin) {
        String content = origin.logContent();
        reqStatisticLog.info(content);
    }

    //-------------------------------------------------------------------------------------------------------

    private void logRespStatistic() {
        ProtocolStatisticHolder origin = respHolder;
        respHolder = new ProtocolStatisticHolder();
        logRespStatistic0(origin);
    }

    private void logRespStatistic0(ProtocolStatisticHolder origin) {
        if (origin != null) {
            if (origin.getRef() > 0) {
                executionContext.schedule(() -> logRespStatistic0(origin), 50, TimeUnit.MILLISECONDS);
                return;
            }
            logRespStatistic1(origin);
        }
    }

    private void logRespStatistic1(ProtocolStatisticHolder origin) {
        String content = origin.logContent();
        respStatisticLog.info(content);
    }

    //-------------------------------------------------------------------------------------------------------

    public void statisticReq(String uuid, long size) {
        reqHolder.reference();
        ProtocolStatistic statistic = reqHolder.getstatistic(uuid);
        statistic.incr(size);
        reqHolder.release();
    }

    public void statisticResp(String uuid, long size) {
        respHolder.reference();
        ProtocolStatistic statistic = respHolder.getstatistic(uuid);
        statistic.incr(size);
        respHolder.release();
    }
}
