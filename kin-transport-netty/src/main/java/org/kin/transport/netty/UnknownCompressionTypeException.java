package org.kin.transport.netty;

/**
 * 未知压缩类型异常
 *
 * @author huangjianqin
 * @date 2020/9/28
 */
public class UnknownCompressionTypeException extends RuntimeException {
    private static final long serialVersionUID = 5185338961508777687L;

    public UnknownCompressionTypeException(String typeName) {
        super(String.format("unknown compression type '%s'", typeName));
    }

    public UnknownCompressionTypeException(int id) {
        super(String.format("unknown compression type id '%s'", id));
    }
}
