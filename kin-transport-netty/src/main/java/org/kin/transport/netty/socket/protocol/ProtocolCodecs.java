package org.kin.transport.netty.socket.protocol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.kin.framework.collection.Tuple;
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
import java.util.*;

/**
 * 协议自动生成javassist字节码
 * <p>
 * 集合数组和map在暂不支持嵌套(可以通过vo类型实现)
 * 不支持null 实例, 请开发者自己生成empty 实例
 *
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
            prettyMethodStatement(readMethodBody,
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
            prettyMethodStatement(readMethodBody, "throw new UnsupportedOperationException();");
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
            prettyMethodStatement(readVOMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sinkName)
                            .concat(" = new ")
                            .concat(target.getName())
                            .concat("();"));

            for (Field field : validField) {
                addFieldRead(readVOMethodBody, sinkName, "$1", target, field);
            }
            prettyMethodStatement(readVOMethodBody, "return msg;");
        } else {
            prettyMethodStatement(readVOMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(readVOMethodBody);

        log.debug(readVOMethodBody.toString());

        CtMethod readCtMethod = CtMethod.make(readVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(readCtMethod);
    }

    /**
     * 所有成员域set方法
     *
     * @param sinkName   协议
     * @param sourceName request bytes
     */
    private static void addFieldRead(StringBuilder sb, String sinkName, String sourceName, Class<?> target, Field field) {
        //setter
        Method setterMethod = ClassUtils.setterMethod(target, field);

        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            //数组类型
            readArrayFieldStatement(sb, field, sinkName, sourceName, setterMethod.getName());
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            //集合类型
            readCollectionFieldStatement(sb, field, sinkName, sourceName, setterMethod.getName());
        } else if (Map.class.isAssignableFrom(fieldType)) {
            //map类型
            readMapFieldStatement(sb, field, sinkName, sourceName, setterMethod.getName());
        } else {
            //其他类型
            prettyMethodStatement(sb, setFieldStatement(sinkName, setterMethod.getName(), readStatement(sourceName, fieldType)));
        }
    }

    /**
     * 生成 sinkName.setXX(readSource) 代码
     */
    private static String setFieldStatement(String sinkName, String sourceSetterMethod, String readSource) {
        return sinkName
                .concat(".")
                .concat(sourceSetterMethod)
                .concat("(")
                .concat(readSource)
                .concat(");");
    }

    /**
     * 根据类型生成 sourceName.readXX() | ProtocolCodecs.codec(XXX.class).readVO(sourceName) 代码, 这里的类型不包括数组, 集合和map
     */
    private static String readStatement(String sourceName, Class<?> type) {
        ProtocolVO protocolVO = type.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
                return readCommon("int", sourceName);
            } else {
                return readCommon(type.getSimpleName(), sourceName);
            }
        } else {
            //vo
            return readVO(type, sourceName);
        }
    }

    /**
     * 根据类型生成 (Cast)(sourceName.readXX() | ProtocolCodecs.codec(XXX.class).readVO(sourceName)) 代码, 这里的类型不包括数组, 集合和map
     */
    private static String readStatementWithCast(String sourceName, Class<?> type) {
        ProtocolVO protocolVO = type.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (Integer.class.equals(type)) {
                return ClassUtils.primitivePackage(Integer.TYPE, readCommon("int", sourceName));
            } else if (Short.class.equals(type)) {
                return ClassUtils.primitivePackage(Short.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else if (Byte.class.equals(type)) {
                return ClassUtils.primitivePackage(Byte.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else if (Long.class.equals(type)) {
                return ClassUtils.primitivePackage(Long.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else if (Float.class.equals(type)) {
                return ClassUtils.primitivePackage(Float.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else if (Double.class.equals(type)) {
                return ClassUtils.primitivePackage(Double.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else if (Character.class.equals(type)) {
                return ClassUtils.primitivePackage(Character.TYPE, readCommon(type.getSimpleName(), sourceName));
            } else {
                return readCommon(type.getSimpleName(), sourceName);
            }
        } else {
            //vo
            return "(".concat(type.getName()).concat(")").concat(readVO(type, sourceName));
        }
    }

    /**
     * 生成 sourceName.readXX() 代码
     */
    private static String readCommon(String typeName, String sourceName) {
        return sourceName
                .concat(".read")
                .concat(StringUtils.firstUpperCase(typeName))
                .concat("()");
    }

    /**
     * 生成 ProtocolCodecs.codec(XXX.class).readVO(sourceName) 代码
     */
    private static String readVO(Class<?> type, String sourceName) {
        return "(".concat(type.getName())
                .concat(")")
                .concat(ProtocolCodecs.class.getName())
                .concat(".codec(")
                .concat(type.getName())
                .concat(".class).readVO(")
                .concat(sourceName)
                .concat(")");
    }

    /**
     * read array
     */
    private static void readArrayFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceSetterMethod) {
        //数组item类型
        Class<?> itemType = ClassUtils.getItemType(field);
        if (isInited(itemType)) {
            //初始化数组类型codec
            init(itemType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(readStatement(sourceName, Short.class))
                        .concat(";"));
        //数组变量名
        String arrVar = field.getName();
        prettyMethodStatement(sb,
                itemType.getName()
                        .concat("[] ")
                        .concat(arrVar)
                        .concat(" = new ")
                        .concat(itemType.getName())
                        .concat("[")
                        .concat(sizeVar)
                        .concat("];"));
        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "for(int i = 0; i < ".concat(sizeVar).concat("; i++)"));
        if (itemType.isPrimitive()) {
            prettyForStatement(forSb,
                    arrVar.concat("[i] = ")
                            .concat(readStatement(sourceName, itemType))
                            .concat(";"));
        } else {
            prettyForStatement(forSb,
                    arrVar.concat("[i] = ")
                            .concat(readStatementWithCast(sourceName, itemType))
                            .concat(";"));
        }

        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
        //set
        prettyMethodStatement(sb, setFieldStatement(sinkName, sourceSetterMethod, arrVar));
    }

    /**
     * read collection
     */
    private static void readCollectionFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceSetterMethod) {
        //集合类型
        Class<?> fieldType = field.getType();
        //集合item类型
        Class<?> itemType = ClassUtils.getItemType(field);
        if (isInited(itemType)) {
            //初始化集合
            init(itemType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(readStatement(sourceName, Short.class))
                        .concat(";"));

        //集合变量名
        String collectionVar = field.getName();
        if (!Modifier.isAbstract(fieldType.getModifiers())) {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(collectionVar)
                            .concat(" = new ")
                            .concat(fieldType.getName())
                            .concat(";"));
        } else if (List.class.isAssignableFrom(fieldType)) {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(collectionVar)
                            .concat(" = new ")
                            .concat(ArrayList.class.getName())
                            .concat("(")
                            .concat(sizeVar)
                            .concat(");"));
        } else if (Set.class.isAssignableFrom(fieldType)) {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(collectionVar)
                            .concat(" = new ")
                            .concat(HashSet.class.getName())
                            .concat("(")
                            .concat(sizeVar)
                            .concat(");"));
        } else if (Queue.class.isAssignableFrom(fieldType)) {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(collectionVar)
                            .concat(" = new ")
                            .concat(LinkedList.class.getName())
                            .concat("();"));
        } else {
            throw new UnsupportedOperationException();
        }

        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "for(int i = 0; i < ".concat(sizeVar).concat("; i++)"));
        prettyForStatement(forSb,
                collectionVar.concat(".add(")
                        .concat(readStatementWithCast(sourceName, itemType))
                        .concat(");"));
        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
        //set
        prettyMethodStatement(sb, setFieldStatement(sinkName, sourceSetterMethod, collectionVar));
    }

    /**
     * read map
     */
    private static void readMapFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceSetterMethod) {
        //map类型
        Class<?> fieldType = field.getType();
        //map entry类型
        Tuple<Class<?>, Class<?>> kvType = ClassUtils.getKVType(field);
        Class<?> keyType = kvType.first();
        Class<?> valueType = kvType.second();
        if (isInited(keyType)) {
            //初始化数组类型codec
            init(keyType, false);
        }

        if (isInited(valueType)) {
            //初始化数组类型codec
            init(valueType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(readStatement(sourceName, Short.class))
                        .concat(";"));
        //集合变量名
        String mapVar = field.getName();
        if (!Modifier.isAbstract(fieldType.getModifiers())) {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(mapVar)
                            .concat(" = new ")
                            .concat(fieldType.getName())
                            .concat(";"));
        } else {
            prettyMethodStatement(sb,
                    fieldType.getName()
                            .concat(" ")
                            .concat(mapVar)
                            .concat(" = new ")
                            .concat(HashMap.class.getName())
                            .concat("(")
                            .concat(sizeVar)
                            .concat(");"));
        }

        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "for(int i = 0; i < ".concat(sizeVar).concat("; i++)"));
        prettyForStatement(forSb,
                mapVar.concat(".put(")
                        .concat(readStatementWithCast(sourceName, keyType))
                        .concat(",")
                        .concat(readStatementWithCast(sourceName, valueType))
                        .concat(");"));
        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
        //set
        prettyMethodStatement(sb, setFieldStatement(sinkName, sourceSetterMethod, mapVar));
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
            prettyMethodStatement(writeMethodBody,
                    target.getName()
                            .concat(" ")
                            .concat(sourceName)
                            .concat(" = (")
                            .concat(target.getName())
                            .concat(")$1;"));
            prettyMethodStatement(writeMethodBody,
                    SocketResponseOprs.class.getName()
                            .concat(" ")
                            .concat(sinkName)
                            .concat(" = new ")
                            .concat(SocketProtocolByteBuf.class.getName())
                            .concat("(protocol.getProtocolId());"));

            for (Field field : validField) {
                addFieldWrite(writeMethodBody, sinkName, sourceName, target, field);
            }

            prettyMethodStatement(writeMethodBody, "return response;");
        } else {
            prettyMethodStatement(writeMethodBody, "throw new UnsupportedOperationException();");
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
            prettyMethodStatement(writeVOMethodBody,
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
            prettyMethodStatement(writeVOMethodBody, "throw new UnsupportedOperationException();");
        }
        prettyMethodTail(writeVOMethodBody);

        log.debug(writeVOMethodBody.toString());

        CtMethod writeCtMethod = CtMethod.make(writeVOMethodBody.toString(), codecCtClass);
        codecCtClass.addMethod(writeCtMethod);
    }

    /**
     * 每个成员域的编码方法
     *
     * @param sinkName   response bytes
     * @param sourceName 协议
     */
    private static void addFieldWrite(StringBuilder sb, String sinkName, String sourceName, Class<?> target, Field field) {
        //getter
        Method getterMethod = ClassUtils.getterMethod(target, field);

        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            //数组类型
            writeArrayFieldStatement(sb, field, sinkName, sourceName, getterMethod.getName());
        } else if (Collection.class.isAssignableFrom(fieldType)) {
            //集合类型
            writeCollectionFieldStatement(sb, field, sinkName, sourceName, getterMethod.getName());
        } else if (Map.class.isAssignableFrom(fieldType)) {
            //map类型
            writeMapFieldStatement(sb, field, sinkName, sourceName, getterMethod.getName());
        } else {
            //其他类型
            prettyMethodStatement(sb, writeFieldStatement(sinkName, sourceName, getterMethod.getName(), fieldType));
        }
    }

    /**
     * 生成 sinkName.writeXXX(sourceName.getXXX()) | ProtocolCodecs.codec(XXX.class).writeVO(source, sinkName) 代码, 这里的类型不包括数组, 集合和map
     */
    private static String writeFieldStatement(String sinkName, String sourceName, String sourceGetterMethod, Class<?> fieldType) {
        String source = sourceName
                .concat(".")
                .concat(sourceGetterMethod)
                .concat("()");

        ProtocolVO protocolVO = fieldType.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (Integer.class.equals(fieldType) || Integer.TYPE.equals(fieldType)) {
                return writeCommon(sinkName, "int", source);
            } else {
                return writeCommon(sinkName, fieldType.getSimpleName(), source);
            }
        } else {
            //vo
            return writeVO(sinkName, source, fieldType);
        }
    }

    /**
     * 生成 sinkName.writeXXX(source) 代码
     */
    private static String writeCommon(String sinkName, String typeName, String source) {
        return sinkName.concat(".write")
                .concat(StringUtils.firstUpperCase(typeName))
                .concat("(")
                .concat(source)
                .concat(");");
    }

    /**
     * 生成 sinkName.writeXXX(source) 代码
     */
    private static String writeCommon(String sinkName, Class<?> type, String source) {
        String typeName = type.getSimpleName();
        if (Integer.class.equals(type)) {
            typeName = "int";
        }
        return sinkName.concat(".write")
                .concat(StringUtils.firstUpperCase(typeName))
                .concat("(")
                .concat(source)
                .concat(");");
    }

    /**
     * 生成 ProtocolCodecs.codec(XXX.class).writeVO(source, sinkName) 代码
     */
    private static String writeVO(String sinkName, String source, Class<?> type) {
        return ProtocolCodecs.class.getName()
                .concat(".codec(")
                .concat(type.getName())
                .concat(".class).writeVO(")
                .concat(source)
                .concat(", ")
                .concat(sinkName)
                .concat(");");
    }

    /**
     * 处理拆箱和vo强转
     * 生成Integer.valueOf(source) | (XXX.class)source 代码
     */
    private static String unpackageGetStatement(String source, Class<?> type) {

        ProtocolVO protocolVO = type.getAnnotation(ProtocolVO.class);
        if (Objects.isNull(protocolVO)) {
            //基础类型
            if (Integer.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Integer.TYPE, source);
            } else if (Short.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Short.TYPE, source);
            } else if (Byte.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Byte.TYPE, source);
            } else if (Long.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Long.TYPE, source);
            } else if (Float.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Float.TYPE, source);
            } else if (Double.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Double.TYPE, source);
            } else if (Character.class.equals(type)) {
                return ClassUtils.primitiveUnpackage(Character.TYPE, source);
            } else {
                return source;
            }
        } else {
            //vo
            return "(".concat(type.getName()).concat(")").concat(source);
        }
    }

    /**
     * write array
     */
    private static void writeArrayFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceGetterMethod) {
        Class<?> fieldType = field.getType();
        //数组item类型
        Class<?> itemType = ClassUtils.getItemType(field);
        if (isInited(itemType)) {
            //初始化数组类型codec
            init(itemType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(sourceName)
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("().length;"));
        prettyMethodStatement(sb, writeCommon(sinkName, Short.class.getSimpleName(), sizeVar));

        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "for(int i = 0; i < ".concat(sizeVar).concat("; i++)"));
        //获取array item代码
        String source = unpackageGetStatement(
                sourceName
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("()[i]"),
                itemType);
        if (itemType.getAnnotation(ProtocolVO.class) != null) {
            prettyForStatement(forSb,
                    writeVO(sinkName,
                            source, itemType));
        } else {
            prettyForStatement(forSb,
                    writeCommon(sinkName,
                            itemType,
                            source));
        }
        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
    }

    /**
     * write collection
     */
    private static void writeCollectionFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceGetterMethod) {
        //集合item类型
        Class<?> itemType = ClassUtils.getItemType(field);
        if (isInited(itemType)) {
            //初始化集合
            init(itemType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(sourceName)
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("().size();"));
        prettyMethodStatement(sb, writeCommon(sinkName, Short.class.getSimpleName(), sizeVar));

        //iterator
        String iteratorVar = field.getName().concat("Iterator");
        prettyMethodStatement(sb,
                Iterator.class.getName()
                        .concat(" ")
                        .concat(iteratorVar)
                        .concat(" = ")
                        .concat(sourceName)
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("().iterator();"));

        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "while(".concat(iteratorVar).concat(".hasNext())"));
        //获取集合 item代码
        String source = unpackageGetStatement(iteratorVar.concat(".next()"), itemType);
        if (itemType.getAnnotation(ProtocolVO.class) != null) {
            prettyForStatement(forSb,
                    writeVO(sinkName,
                            source, itemType));
        } else {
            prettyForStatement(forSb,
                    writeCommon(sinkName,
                            itemType,
                            source));
        }
        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
    }

    /**
     * write map
     */
    private static void writeMapFieldStatement(StringBuilder sb, Field field, String sinkName, String sourceName, String sourceGetterMethod) {
        //map entry类型
        Tuple<Class<?>, Class<?>> kvType = ClassUtils.getKVType(field);
        Class<?> keyType = kvType.first();
        Class<?> valueType = kvType.second();
        if (isInited(keyType)) {
            //初始化数组类型codec
            init(keyType, false);
        }

        if (isInited(valueType)) {
            //初始化数组类型codec
            init(valueType, false);
        }

        //大小为short
        //size变量名
        String sizeVar = field.getName().concat("Size");
        prettyMethodStatement(sb,
                "int ".concat(sizeVar)
                        .concat(" = ")
                        .concat(sourceName)
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("().size();"));
        prettyMethodStatement(sb, writeCommon(sinkName, Short.class.getSimpleName(), sizeVar));

        //iterator
        String iteratorVar = field.getName().concat("Iterator");
        prettyMethodStatement(sb,
                Iterator.class.getName()
                        .concat(" ")
                        .concat(iteratorVar)
                        .concat(" = ")
                        .concat(sourceName)
                        .concat(".")
                        .concat(sourceGetterMethod)
                        .concat("().entrySet().iterator();"));

        //for循环设置变量值
        StringBuilder forSb = new StringBuilder();
        prettyForHead(forSb, "while(".concat(iteratorVar).concat(".hasNext())"));
        //获取map entry代码
        String source = iteratorVar
                .concat(".next()");
        String entryName = "entry";
        prettyForStatement(forSb,
                Map.Entry.class.getName()
                        .concat(" ")
                        .concat(entryName)
                        .concat(" = ")
                        .concat(source)
                        .concat(";"));
        String keySource = unpackageGetStatement(entryName.concat(".getKey()"), keyType);
        if (keyType.getAnnotation(ProtocolVO.class) != null) {
            prettyForStatement(forSb,
                    writeVO(sinkName,
                            keySource, keyType));
        } else {
            prettyForStatement(forSb,
                    writeCommon(sinkName,
                            keyType,
                            keySource));
        }

        String valueSource = unpackageGetStatement(entryName.concat(".getValue()"), valueType);
        if (valueType.getAnnotation(ProtocolVO.class) != null) {
            prettyForStatement(forSb,
                    writeVO(sinkName,
                            valueSource, valueType));
        } else {
            prettyForStatement(forSb,
                    writeCommon(sinkName,
                            valueType,
                            valueSource));
        }
        prettyForTail(forSb);
        //for循环代码
        sb.append(forSb.toString());
    }
    //------------------------------------------------------------------------------------------------------------------------------------

    /**
     * 优雅格式化方法体每行代码
     */
    private static void prettyMethodStatement(StringBuilder sb, String content) {
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

    /**
     * 优雅格式化循环体每行代码
     */
    private static void prettyForStatement(StringBuilder sb, String content) {
        sb.append("  ".concat(content).concat(System.lineSeparator()));
    }

    /**
     * 优雅格式化循环头
     */
    private static void prettyForHead(StringBuilder sb, String content) {
        sb.append(" ".concat(content).concat("{").concat(System.lineSeparator()));
    }

    /**
     * 优雅格式化循环头
     */
    private static void prettyForTail(StringBuilder sb) {
        sb.append(" }".concat(System.lineSeparator()));
    }
}
