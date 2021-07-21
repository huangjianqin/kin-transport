package org.kin.transport.netty.socket.protocol;

import org.kin.framework.utils.ClassScanUtils;
import org.kin.framework.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 协议生成工厂
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class ProtocolFactory {
    private static final Logger log = LoggerFactory.getLogger(ProtocolFactory.class);
    /** key -> 协议id, value -> 协议信息 */
    private static final ConcurrentHashMap<Integer, ProtocolInfo> ID_2_PROTOCOL_CLASS = new ConcurrentHashMap<>();
    /** key -> 协议实现类, value -> 协议id */
    private static final ConcurrentHashMap<Class<? extends SocketProtocol>, Integer> PROTOCOL_CLASS_2_ID = new ConcurrentHashMap<>();

    private ProtocolFactory() {
    }

    /**
     * 仅仅append
     *
     * @param scanPath 扫描package
     */
    @SuppressWarnings("unchecked")
    public static void init(String scanPath) {
        synchronized (ProtocolFactory.class) {

            List<Class<?>> protocolClasses = ClassScanUtils.scan(scanPath, SocketProtocol.class);
            for (Class<?> protocolClass : protocolClasses) {
                Protocol protocolAnnotation = protocolClass.getAnnotation(Protocol.class);
                if (protocolAnnotation != null) {
                    int rate = protocolAnnotation.rate();
                    int protocolId = protocolAnnotation.id();
                    ID_2_PROTOCOL_CLASS.put(protocolId, new ProtocolInfo((Class<? extends SocketProtocol>) protocolClass, rate));
                    PROTOCOL_CLASS_2_ID.put((Class<? extends SocketProtocol>) protocolClass, protocolId);
                    log.info("find protocol(id={}) >>> {}, rate={}", protocolId, protocolClass, rate);
                }
            }
            ProtocolCodecs.init(scanPath);
        }
    }

    /**
     * 根据id创建protocol实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends SocketProtocol> T createProtocol(int id) {
        Class<? extends SocketProtocol> claxx = getSocketProtocolClass(id);
        if (Objects.nonNull(claxx)) {
            return (T) ClassUtils.instance(claxx);
        }

        throw new ProtocolException("unknow protocol '" + id + "'");
    }

    /**
     * 获取协议限流流量
     *
     * @param id 协议id
     */
    public static int getProtocolRate(int id) {
        ProtocolInfo protocolInfo = ID_2_PROTOCOL_CLASS.get(id);
        if (protocolInfo != null) {
            return protocolInfo.getRate();
        }
        //没有该协议, 返回最大协议间隔, 也就意味着直接抛弃
        return Integer.MAX_VALUE;
    }

    /**
     * 根据id获取protocol实现类
     */
    public static Class<? extends SocketProtocol> getSocketProtocolClass(int id) {
        ProtocolInfo protocolInfo = ID_2_PROTOCOL_CLASS.get(id);
        return Objects.nonNull(protocolInfo) ? protocolInfo.getProtocolClass() : null;
    }

    /**
     * 获取所有已注册的protocol实现类
     */
    public static List<Class<? extends SocketProtocol>> getSocketProtocolClasses() {
        return ID_2_PROTOCOL_CLASS.values().stream().map(ProtocolInfo::getProtocolClass).collect(Collectors.toList());
    }

    /**
     * 根据protocol实现类获取id
     */
    public static Integer getProtocolId(Class<? extends SocketProtocol> claxx) {
        return PROTOCOL_CLASS_2_ID.get(claxx);
    }

    //------------------------------------------------------------------------------------------------------
    private static class ProtocolInfo {
        /** 协议类 */
        private final Class<? extends SocketProtocol> protocolClass;
        /** 协议限流流量 */
        private final int rate;

        public ProtocolInfo(Class<? extends SocketProtocol> protocolClass, int rate) {
            this.protocolClass = protocolClass;
            this.rate = rate;
        }

        //getter
        public Class<? extends SocketProtocol> getProtocolClass() {
            return protocolClass;
        }

        public int getRate() {
            return rate;
        }
    }
}
