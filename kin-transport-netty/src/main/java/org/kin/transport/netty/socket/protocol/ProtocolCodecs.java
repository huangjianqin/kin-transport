package org.kin.transport.netty.socket.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

    static {
        POOL.importPackage("org.kin");
    }

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
                init(fieldType, false);
            }

            validField.add(field);
        }

        //codec类生成
        try {
            String codecCtClassName = target.getName().concat("Codec");
            CtClass codecCtClass = POOL.makeClass(codecCtClassName);
            codecCtClass.addInterface(POOL.getCtClass(ProtocolCodec.class.getName()));

            log.debug(System.lineSeparator());
            log.debug("#############".concat(codecCtClassName).concat("#############").concat(System.lineSeparator()));

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

            log.debug("##########################".concat(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加解析协议方法
     */
    private static void addReadMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method readMethod = ProtocolCodec.class.getMethod("read", SocketRequestOprs.class, SocketProtocol.class);
        StringBuilder readMethodBody = new StringBuilder();
        prettyMethodHead(readMethodBody, ClassUtils.generateMethodDeclaration(readMethod));
        if (isProtocol) {
            String sinkName = "protocol";
            prettyStatement(readMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sinkName)
                            .concat(" = (")
                            .concat(target.getName())
                            .concat(")")
                            .concat("$2;"));

            for (Field field : validField) {
                addFieldRead(readMethodBody, sinkName, "$1", target, field);
            }
        } else {
            prettyStatement(readMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(readMethodBody);

        log.debug(readMethodBody.toString());

        CtMethod readCtMethod = CtMethod.make(readMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(readCtMethod);
    }

    /**
     * 添加解析VO方法
     */
    private static void addReadVOMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method readVOMethod = ProtocolCodec.class.getMethod("readVO", SocketRequestOprs.class);
        StringBuilder readVOMethodBody = new StringBuilder();
        prettyMethodHead(readVOMethodBody, ClassUtils.generateMethodDeclaration(readVOMethod));
        if (!isProtocol) {
            String sinkName = "msg";
            prettyStatement(readVOMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sinkName)
                            .concat(" = new ")
                            .concat(target.getName())
                            .concat("();"));

            for (Field field : validField) {
                addFieldRead(readVOMethodBody, sinkName, "$1", target, field);
            }
            prettyStatement(readVOMethodBody, "return msg;");
        } else {
            prettyStatement(readVOMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(readVOMethodBody);

        log.debug(readVOMethodBody.toString());

        CtMethod readCtMethod = CtMethod.make(readVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(readCtMethod);
    }

    /**
     * 每个成员域的解析方法
     */
    private static void addFieldRead(StringBuilder sb, String sinkName, String sourceName, Class<?> target, Field field) {
        //setter
        Method setterMethod = ClassUtils.setterMethod(target, field);

        Class<?> fieldType = field.getType();
        ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (String.class.equals(fieldType)) {
                BigString bigString = field.getAnnotation(BigString.class);
                if (bigString != null) {
                    prettyStatement(sb, addFieldReadMethod(BigString.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
                } else {
                    prettyStatement(sb, addFieldReadMethod(String.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
                }
            } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Boolean.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Byte.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Short.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod("int", sinkName, sourceName, setterMethod.getName()));
            } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Long.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Float.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                prettyStatement(sb, addFieldReadMethod(Double.class.getSimpleName(), sinkName, sourceName, setterMethod.getName()));
            }
        } else {
            //vo
            prettyStatement(sb,
                    sinkName.concat(".")
                            .concat(setterMethod.getName())
                            .concat("((")
                            .concat(fieldType.getName())
                            .concat(")")
                            .concat(ProtocolCodecs.class.getName())
                            .concat(".codec(")
                            .concat(field.getType().getName())
                            .concat(".class).readVO(")
                            .concat(sourceName)
                            .concat(")")
                            .concat(");"));
        }
    }

    /**
     * 每个成员域的解析方法
     */
    private static String addFieldReadMethod(String typeName, String sinkName, String sourceName, String sourceSetterMethod) {
        return sinkName
                .concat(".")
                .concat(sourceSetterMethod)
                .concat("(")
                .concat(sourceName)
                .concat(".read")
                .concat(StringUtils.firstUpperCase(typeName))
                .concat("()")
                .concat(");");
    }

    /**
     * 添加协议编码方法
     */
    private static void addWriteMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method writeMethod = ProtocolCodec.class.getMethod("write", SocketProtocol.class);
        StringBuilder writeMethodBody = new StringBuilder();
        prettyMethodHead(writeMethodBody, ClassUtils.generateMethodDeclaration(writeMethod));
        if (isProtocol) {
            String sinkName = "response";
            String sourceName = "protocol";
            prettyStatement(writeMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sourceName)
                            .concat(" = (")
                            .concat(target.getName())
                            .concat(")$1;"));
            prettyStatement(writeMethodBody,
                    SocketResponseOprs.class.getName()
                            .concat(" ")
                            .concat(sinkName)
                            .concat(" = new ")
                            .concat(SocketProtocolByteBuf.class.getName())
                            .concat("(protocol.getProtocolId());"));

            for (Field field : validField) {
                addFieldWrite(writeMethodBody, sinkName, sourceName, target, field);
            }

            prettyStatement(writeMethodBody, "return response;");
        } else {
            prettyStatement(writeMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(writeMethodBody);

        log.debug(writeMethodBody.toString());

        CtMethod writeCtMethod = CtMethod.make(writeMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(writeCtMethod);
    }

    /**
     * 添加VO编码方法
     */
    private static void addWriteVOMethod(CtClass codecCtClass, Class<?> target, List<Field> validField, boolean isProtocol) throws NoSuchMethodException, CannotCompileException {
        Method writeVOMethod = ProtocolCodec.class.getMethod("writeVO", Object.class, SocketResponseOprs.class);
        StringBuilder writeVOMethodBody = new StringBuilder();
        prettyMethodHead(writeVOMethodBody, ClassUtils.generateMethodDeclaration(writeVOMethod));
        if (!isProtocol) {
            String sinkName = "$2";
            String sourceName = "msg";
            prettyStatement(writeVOMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sourceName)
                            .concat(" = (")
                            .concat(target.getName())
                            .concat(")$1;"));

            for (Field field : validField) {
                addFieldWrite(writeVOMethodBody, sinkName, sourceName, target, field);
            }
        } else {
            prettyStatement(writeVOMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(writeVOMethodBody);

        log.debug(writeVOMethodBody.toString());

        CtMethod writeCtMethod = CtMethod.make(writeVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(writeCtMethod);
    }

    /**
     * 每个成员域的编码方法
     */
    private static void addFieldWrite(StringBuilder sb, String sinkName, String sourceName, Class<?> target, Field field) {

        //getter
        Method getterMethod = ClassUtils.getterMethod(target, field);

        Class<?> fieldType = field.getType();
        ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (String.class.equals(fieldType)) {
                BigString bigString = field.getAnnotation(BigString.class);
                if (bigString != null) {
                    prettyStatement(sb,
                            sinkName.concat(addFieldWriteMethod(BigString.class.getSimpleName(), sourceName, getterMethod.getName())));
                } else {
                    prettyStatement(sb,
                            sinkName.concat(addFieldWriteMethod(String.class.getSimpleName(), sourceName, getterMethod.getName())));
                }
            } else if (Boolean.class.equals(fieldType) || Boolean.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Boolean.class.getSimpleName(), sourceName, getterMethod.getName())));
            } else if (Byte.class.equals(fieldType) || Byte.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Byte.class.getSimpleName(), sourceName, getterMethod.getName())));
            } else if (Short.class.equals(fieldType) || Short.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Short.class.getSimpleName(), sourceName, getterMethod.getName())));
            } else if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod("int", sourceName, getterMethod.getName())));
            } else if (Long.class.equals(fieldType) || Long.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Long.class.getSimpleName(), sourceName, getterMethod.getName())));
            } else if (Float.class.equals(fieldType) || Float.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Float.class.getSimpleName(), sourceName, getterMethod.getName())));
            } else if (Double.class.equals(fieldType) || Double.TYPE.equals(fieldType)) {
                prettyStatement(sb,
                        sinkName.concat(addFieldWriteMethod(Double.class.getSimpleName(), sourceName, getterMethod.getName())));
            }
        } else {
            //vo
            prettyStatement(sb,
                    ProtocolCodecs.class.getName()
                            .concat(".codec(")
                            .concat(field.getType().getName())
                            .concat(".class).writeVO(")
                            .concat(sourceName)
                            .concat(".")
                            .concat(getterMethod.getName())
                            .concat("(), ")
                            .concat(sinkName)
                            .concat(");"));
        }
    }

    /**
     * 每个成员域的编码方法
     */
    private static String addFieldWriteMethod(String typeName, String sourceName, String sourceGetterMethod) {
        return ".write"
                .concat(StringUtils.firstUpperCase(typeName))
                .concat("(")
                .concat(sourceName)
                .concat(".")
                .concat(sourceGetterMethod)
                .concat("());");
    }

    //------------------------------------------------------------------------------------------------------------------------------------

    /**
     * 优雅格式化方法体每行代码
     */
    private static void prettyStatement(StringBuilder sb, String content) {
        sb.append(" ".concat(content).concat(System.lineSeparator()));
    }

    /**
     * 优雅格式化方法头
     */
    private static void prettyMethodHead(StringBuilder sb, String content) {
        sb.append(content.concat("{").concat(System.lineSeparator()));
    }

    /**
     * 优雅格式化方法头
     */
    private static void prettyMethodTail(StringBuilder sb) {
        sb.append("}".concat(System.lineSeparator()));
    }
}
