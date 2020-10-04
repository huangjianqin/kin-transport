package org.kin.transport.netty.socket.protocol;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author huangjianqin
 * @date 2020/10/4
 */
class ProtocolUtils {
    /**
     * @return 该成员域是否有效
     */
    static boolean isFieldValid(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isFinal(modifiers) ||
                Modifier.isPublic(modifiers) ||
                Modifier.isStatic(modifiers) ||
                Modifier.isTransient(modifiers)) {
            //忽略final public static transient
            return false;
        }

        return true;
    }
}
