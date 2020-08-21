package org.kin.transport.netty.socket.domain;

/**
 * session close 原因
 *
 * @author huangjianqin
 * @date 2019/6/3
 */
public enum SessionCloseCause {
    /**
     * 维护
     */
    MAINTAIN,
    /**
     * 空闲超时
     */
    IDLE_TIMEOUT,
    /**
     * 网络错误
     */
    NETWORK_ERR,
    /**
     * 协议包太多
     */
    PACKET_TOO_MORE,
    ;
}
