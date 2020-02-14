package org.kin.transport.netty.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.ExceptionUtils;
import org.kin.transport.netty.core.exception.UnknowProtocolException;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
public class ProtocolFactory {
    private static final Logger log = LoggerFactory.getLogger(ProtocolFactory.class);
    private static final Cache<Integer, ProtocolInfo> PROTOCOL_CACHE = CacheBuilder.newBuilder().build();

    private ProtocolFactory() {
    }

    /**
     * 仅仅append
     */
    public static void init(String scanPath) {
        synchronized (ProtocolFactory.class) {
            Set<Class<? extends AbstractProtocol>> protocolClasses = ClassUtils.getSubClass(scanPath, AbstractProtocol.class, true);
            for (Class<? extends AbstractProtocol> protocolClass : protocolClasses) {
                Protocol protocolAnnotation = protocolClass.getAnnotation(Protocol.class);
                if (protocolAnnotation != null) {
                    long rate = protocolAnnotation.rate();
                    PROTOCOL_CACHE.put(protocolAnnotation.id(), new ProtocolInfo(protocolClass, rate, protocolAnnotation.callback()));
                    log.info("find protocol(id={}) >>> {}, interval={}, callback={}", protocolAnnotation.id(), protocolClass, rate, protocolAnnotation.callback());
                }
            }
        }
    }

    /**
     * 根据id创建protocol, 并依照field定义顺序设置field value, 从子类开始算
     */
    public static <T extends AbstractProtocol> T createProtocol(int id, Object... fieldValues) {
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            Class<? extends AbstractProtocol> claxx = protocolInfo.getProtocolClass();
            AbstractProtocol protocol = ClassUtils.instance(claxx);
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

            return null;
        }

        throw new UnknowProtocolException(id);
    }

    public static long getProtocolRate(int id){
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            return protocolInfo.getRate();
        }
        /** 没有该协议, 返回最大协议间隔, 也就意味着直接抛弃 */
        return Long.MAX_VALUE;
    }

    public static ProtocolRateLimitCallback getProtocolRateLimitCallback(int id){
        ProtocolInfo protocolInfo = PROTOCOL_CACHE.getIfPresent(id);
        if (protocolInfo != null) {
            return protocolInfo.getRateLimitCallback();
        }
        return null;
    }

    private static class ProtocolInfo {
        private Class<? extends AbstractProtocol> protocolClass;
        private long rate;
        private ProtocolRateLimitCallback rateLimitCallback;

        public ProtocolInfo(Class<? extends AbstractProtocol> protocolClass, long rate, Class<? extends ProtocolRateLimitCallback> rateLimitCallbackClass) {
            this.protocolClass = protocolClass;
            this.rate = rate;
            if (rateLimitCallbackClass != null && ProtocolRateLimitCallback.class.isAssignableFrom(rateLimitCallbackClass)) {
                try {
                    rateLimitCallback = rateLimitCallbackClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    ExceptionUtils.log(e);
                }
            }
        }

        //getter
        public Class<? extends AbstractProtocol> getProtocolClass() {
            return protocolClass;
        }

        public long getRate() {
            return rate;
        }

        public ProtocolRateLimitCallback getRateLimitCallback() {
            return rateLimitCallback;
        }
    }
}
