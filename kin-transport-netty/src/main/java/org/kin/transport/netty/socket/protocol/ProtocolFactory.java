package org.kin.transport.netty.socket.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.kin.framework.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     *
     * @param scanPath 扫描package
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
            ProtocolCodecs.init(scanPath);
        }
    }

    /**
     * 根据id创建protocol, 并依照field定义(父类->子类)顺序设置field value, 从子类开始算
     */
    public static <T extends SocketProtocol> T createProtocol(int id, Object... fieldValues) {
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            Class<? extends SocketProtocol> claxx = protocolInfo.getProtocolClass();
            SocketProtocol protocol = ClassUtils.instance(claxx);
            if (protocol != null) {
                //设置成员域
                Field[] fields = ClassUtils.getAllFields(claxx).toArray(new Field[0]);
                List<Field> validFields = Stream.of(fields).filter(ProtocolUtils::isFieldValid).collect(Collectors.toList());
                Collections.reverse(validFields);

                //设置协议id
                ClassUtils.setFieldValue(protocol, validFields.get(0), id);

                if (fieldValues.length > 0) {
                    for (int i = 0; i < fieldValues.length; i++) {
                        Field field = validFields.get(i + 1);
                        ClassUtils.setFieldValue(protocol, field, fieldValues[i]);
                    }
                }

                return (T) protocol;
            }
        }

        throw new ProtocolException("unknow protocol '" + id + "'");
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
