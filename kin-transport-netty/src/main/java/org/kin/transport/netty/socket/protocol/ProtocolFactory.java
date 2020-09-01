package org.kin.transport.netty.socket.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.kin.framework.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * 协议生成工厂
 *
 * @author huangjianqin
 * @date 2019/7/4
 */
public class ProtocolFactory {
    private static final Logger log = LoggerFactory.getLogger(ProtocolFactory.class);
    /** 协议类信息缓存 */
    private static final Cache<Integer, ProtocolInfo> PROTOCOL_CACHE = CacheBuilder.newBuilder().build();

    private ProtocolFactory() {
    }

    /**
     * 仅仅append
     */
    public static void init(String scanPath) {
        synchronized (ProtocolFactory.class) {
            Set<Class<? extends SocketProtocol>> protocolClasses = ClassUtils.getSubClass(scanPath, SocketProtocol.class, true);
            for (Class<? extends SocketProtocol> protocolClass : protocolClasses) {
                Protocol protocolAnnotation = protocolClass.getAnnotation(Protocol.class);
                if (protocolAnnotation != null) {
                    int rate = protocolAnnotation.rate();
                    PROTOCOL_CACHE.put(protocolAnnotation.id(), new ProtocolInfo(protocolClass, rate));
                    log.info("find protocol(id={}) >>> {}, rate={}", protocolAnnotation.id(), protocolClass, rate);
                }
            }
        }
    }

    /**
     * 根据id创建protocol, 并依照field定义顺序设置field value, 从子类开始算
     */
    public static <T extends SocketProtocol> T createProtocol(int id, Object... fieldValues) {
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            Class<? extends SocketProtocol> claxx = protocolInfo.getProtocolClass();
            SocketProtocol protocol = ClassUtils.instance(claxx);
            if (protocol != null) {
                //设置协议id
                Field[] fields = ClassUtils.getAllFields(claxx).toArray(new Field[0]);
                for (Field field : fields) {
                    if ("protocolId".equals(field.getName())) {
                        ClassUtils.setFieldValue(protocol, field, id);
                        break;
                    }
                }
                if (fieldValues.length > 0) {
                    for (int i = 0; i < fieldValues.length; i++) {
                        Field field = fields[i];
                        ClassUtils.setFieldValue(protocol, field, fieldValues[i]);
                    }
                }

                return (T) protocol;
            }
        }

        throw new UnknownProtocolException(id);
    }

    /**
     * 获取协议限流流量
     *
     * @param id 协议id
     */
    public static int getProtocolRate(int id) {
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            return protocolInfo.getRate();
        }
        //没有该协议, 返回最大协议间隔, 也就意味着直接抛弃
        return Integer.MAX_VALUE;
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
