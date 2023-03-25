package org.kin.transport.netty;

/**
 * @author huangjianqin
 * @date 2023/3/23
 */
@FunctionalInterface
public interface ObjectEncoder<T> {
    /**
     * 对象序列化
     *
     * @param obj             待发送的对象
     * @param outboundPayload 最终发送的outbound payload
     */
    void encode(T obj, ByteBufPayload outboundPayload);
}
