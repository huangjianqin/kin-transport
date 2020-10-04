package org.kin.transport.netty.socket.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.kin.framework.utils.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2020/10/4
 */
public class ProtocolCodecs {
    private static final Logger log = LoggerFactory.getLogger(ProtocolCodecs.class);
    /** javassist class pool */
    private static final ClassPool POOL = ClassPool.getDefault();

    /** {@link ProtocolCodec} 缓存 */
    private static Cache<Class<?>, ProtocolCodec<?>> protocolCodecs = CacheBuilder.newBuilder().build();

    private ProtocolCodecs() {
    }

    //----------------------------------------------------------------------------------------------------------------------------------------

    /**
     * @return 该协议对象是否已初始化
     */
    private static boolean isInited(Class<?> target) {
        return Objects.nonNull(protocolCodecs.getIfPresent(target));
    }

    public static <P> ProtocolCodec<P> codec(Class<P> target) {
        return (ProtocolCodec<P>) protocolCodecs.getIfPresent(target);
    }

    //----------------------------------------------------------------------------------------------------------------------------------------

    /**
     * 初始化某classpath下的协议codec
     */
    static void init(String scanPath) {
        synchronized (ProtocolCodecs.class) {
            Reflections reflections = new Reflections(scanPath, new SubTypesScanner(), new TypeAnnotationsScanner());
            //init protocol class
            for (Class<?> target : reflections.getTypesAnnotatedWith(Protocol.class)) {
                init(target, true);
            }

            //init vo codec
            for (Class<?> target : reflections.getTypesAnnotatedWith(ProtocolVO.class)) {
                init(target, false);
            }
        }
    }

    /**
     * 初始化协议codec
     */
    private static void init(Class<?> target, boolean isProtocol) {
        if (Modifier.isAbstract(target.getModifiers())) {
            return;
        }
        if (isInited(target)) {
            return;
        }
        List<Field> fields = ClassUtils.getAllFields(target);
        Collections.reverse(fields);
        List<Field> validField = new ArrayList<>(fields.size());
        for (Field field : fields) {
            if (!ProtocolUtils.isFieldValid(field)) {
                continue;
            }

            if (Objects.nonNull(field.getAnnotation(Ignore.class))) {
                continue;
            }

            //检查setter getter方式
            Method getterMethod = ClassUtils.getterMethod(target, field);
            if (getterMethod == null) {
                throw new ProtocolException(String.format("%s class field '%s' doesn't have getter method", target.getName(), field.getName()));
            }

            Method setterMethod = ClassUtils.setterMethod(target, field);
            if (setterMethod == null) {
                throw new ProtocolException(String.format("%s class field '%s' doesn't have setter method", target.getName(), field.getName()));
            }

            //检查是否是protocol vo
            Class<?> fieldType = field.getType();
            ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
            if (Objects.nonNull(protocolVO)) {
                //先初始化
                init(fieldType, isProtocol);
            }

            validField.add(field);
        }

        //codec类生成
        try {
            String codecCtClassName = target.getName().concat("Codec");
            CtClass codecCtClass = POOL.makeClass(codecCtClassName);
            codecCtClass.addInterface(POOL.getCtClass(ProtocolCodec.class.getName()));

            //生成read方法
            addReadMethod(codecCtClass, target, validField, isProtocol);

            //生成readVO方法
            addReadVOMethod(codecCtClass, target, validField, isProtocol);

            //生成write方法
            addWriteMethod(codecCtClass, target, validField, isProtocol);

            //生成writeVO方法
            addWriteVOMethod(codecCtClass, target, validField, isProtocol);

            Object instance = codecCtClass.toClass().getConstructor().newInstance();
            protocolCodecs.put(target, (ProtocolCodec<?>) instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addReadMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method readMethod = ProtocolCodec.class.getMethod("read", SocketRequestOprs.class, SocketProtocol.class);
        StringBuilder readMethodBody = new StringBuilder();
        readMethodBody.append(ClassUtils.generateMethodDeclaration(readMethod).concat("{"));
        if (isProtocol) {
            readMethodBody.append(target.getName().concat(" protocol = (".concat(target.getName()).concat(")").concat("$2;")));

            for (Field field : validField) {
                //setter
                Method setterMethod = ClassUtils.setterMethod(target, field);

                Class<?> fieldType = field.getType();
                ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
                if (Objects.isNull(protocolVO)) {
                    //基础类型
                    if (String.class.equals(fieldType)) {
                        BigString bigString = field.getAnnotation(BigString.class);
                        if (bigString != null) {
                            readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readBigString()").concat(");").concat(System.lineSeparator()));
                        } else {
                            readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readString()").concat(");").concat(System.lineSeparator()));
                        }
                    } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readBoolean()").concat(");").concat(System.lineSeparator()));
                    } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readByte()").concat(");").concat(System.lineSeparator()));
                    } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readShort()").concat(");").concat(System.lineSeparator()));
                    } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readInt()").concat(");").concat(System.lineSeparator()));
                    } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readLong()").concat(");").concat(System.lineSeparator()));
                    } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readFloat()").concat(");").concat(System.lineSeparator()));
                    } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                        readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("$1.readDouble()").concat(");").concat(System.lineSeparator()));
                    }
                } else {
                    //vo
                    readMethodBody.append("protocol.".concat(setterMethod.getName()).concat("(").concat("ProtocolCodecs.codec(".concat(field.getType().getName()).concat(").read($1)")).concat(");").concat(System.lineSeparator()));
                }
            }
        } else {
            readMethodBody.append("throw new UnsupportedOperationException();");
        }
        readMethodBody.append("}");

        CtMethod readCtMethod = CtMethod.make(readMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(readCtMethod);
    }

    private static void addReadVOMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method readVOMethod = ProtocolCodec.class.getMethod("readVO", SocketRequestOprs.class);
        StringBuilder readVOMethodBody = new StringBuilder();
        readVOMethodBody.append(ClassUtils.generateMethodDeclaration(readVOMethod).concat("{"));
        if (!isProtocol) {
            readVOMethodBody.append(target.getName().concat(" msg = new ").concat(target.getName()).concat("();").concat(System.lineSeparator()));

            for (Field field : validField) {
                //setter
                Method setterMethod = ClassUtils.setterMethod(target, field);

                Class<?> fieldType = field.getType();
                ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
                if (Objects.isNull(protocolVO)) {
                    //基础类型
                    if (String.class.equals(fieldType)) {
                        BigString bigString = field.getAnnotation(BigString.class);
                        if (bigString != null) {
                            readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readBigString()").concat(");").concat(System.lineSeparator()));
                        } else {
                            readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readString()").concat(");").concat(System.lineSeparator()));
                        }
                    } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readBoolean()").concat(");").concat(System.lineSeparator()));
                    } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readByte()").concat(");").concat(System.lineSeparator()));
                    } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readShort()").concat(");").concat(System.lineSeparator()));
                    } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readInt()").concat(");").concat(System.lineSeparator()));
                    } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readLong()").concat(");").concat(System.lineSeparator()));
                    } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readFloat()").concat(");").concat(System.lineSeparator()));
                    } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                        readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("$1.readDouble()").concat(");").concat(System.lineSeparator()));
                    }
                } else {
                    //vo
                    readVOMethodBody.append("msg.".concat(setterMethod.getName()).concat("(").concat("ProtocolCodecs.codec(".concat(field.getType().getName()).concat(").read($1)")).concat(");").concat(System.lineSeparator()));
                }

                readVOMethodBody.append("return msg;".concat(System.lineSeparator()));
            }
        } else {
            readVOMethodBody.append("throw new UnsupportedOperationException();");
        }
        readVOMethodBody.append("}");

        CtMethod readCtMethod = CtMethod.make(readVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(readCtMethod);
    }

    private static void addWriteMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method writeMethod = ProtocolCodec.class.getMethod("write", SocketProtocol.class);
        StringBuffer writeMethodBody = new StringBuffer();
        writeMethodBody.append(ClassUtils.generateMethodDeclaration(writeMethod).concat("{"));
        if (isProtocol) {
            writeMethodBody.append(target.getName().concat(" protocol = (").concat(target.getName()).concat(")$1;"));
            writeMethodBody.append(SocketResponseOprs.class.getName().concat(" response = new ").concat(SocketProtocolByteBuf.class.getName()).concat("(protocol.getProtocolId());"));

            for (Field field : validField) {
                //setter
                Method getterMethod = ClassUtils.getterMethod(target, field);

                Class<?> fieldType = field.getType();
                ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
                if (Objects.isNull(protocolVO)) {
                    //基础类型
                    if (String.class.equals(fieldType)) {
                        BigString bigString = field.getAnnotation(BigString.class);
                        if (bigString != null) {
                            writeMethodBody.append("response.writeBigString(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                        } else {
                            writeMethodBody.append("response.writeString(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                        }
                    } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeBoolean(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeByte(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeShort(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeInt(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeLong(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeFloat(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                        writeMethodBody.append("response.writeDouble(protocol.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    }
                } else {
                    //vo
                    writeMethodBody.append("ProtocolCodecs.codec(".concat(field.getType().getName()).concat(").writeVO(msg.").concat(getterMethod.getName()).concat(", response);").concat(System.lineSeparator()));
                }
            }

            writeMethodBody.append("return response;");
        } else {
            writeMethodBody.append("throw new UnsupportedOperationException();");
        }
        writeMethodBody.append("}");

        CtMethod writeCtMethod = CtMethod.make(writeMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(writeCtMethod);
    }

    private static void addWriteVOMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method writeVOMethod = ProtocolCodec.class.getMethod("writeVO", Object.class, SocketResponseOprs.class);
        StringBuffer writeVOMethodBody = new StringBuffer();
        writeVOMethodBody.append(ClassUtils.generateMethodDeclaration(writeVOMethod).concat("{"));
        if (!isProtocol) {
            writeVOMethodBody.append(target.getName().concat(" msg = (").concat(target.getName()).concat(")$1;"));

            for (Field field : validField) {
                //setter
                Method getterMethod = ClassUtils.getterMethod(target, field);

                Class<?> fieldType = field.getType();
                ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
                if (Objects.isNull(protocolVO)) {
                    //基础类型
                    if (String.class.equals(fieldType)) {
                        BigString bigString = field.getAnnotation(BigString.class);
                        if (bigString != null) {
                            writeVOMethodBody.append("response.writeBigString(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                        } else {
                            writeVOMethodBody.append("response.writeString(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                        }
                    } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeBoolean(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeByte(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeShort(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeInt(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeLong(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeFloat(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                        writeVOMethodBody.append("response.writeDouble(msg.".concat(getterMethod.getName()).concat("());").concat(System.lineSeparator()));
                    }
                } else {
                    //vo
                    writeVOMethodBody.append("ProtocolCodecs.codec(".concat(field.getType().getName()).concat(").writeVO(msg.").concat(getterMethod.getName()).concat(", response);").concat(System.lineSeparator()));
                }
            }

            writeVOMethodBody.append("return response;");
        } else {
            writeVOMethodBody.append("throw new UnsupportedOperationException();");
        }
        writeVOMethodBody.append("}");

        CtMethod writeCtMethod = CtMethod.make(writeVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(writeCtMethod);
    }
}
