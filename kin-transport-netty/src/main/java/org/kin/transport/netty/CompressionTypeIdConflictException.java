package org.kin.transport.netty;

/**
 * 压缩类型id重复
 *
 * @author huangjianqin
 * @date 2020/9/28
 */
public class CompressionTypeIdConflictException extends RuntimeException {
    private static final long serialVersionUID = 313574421221484546L;

    public CompressionTypeIdConflictException(CompressionType compressionType) {
        super(String.format("compression type conflict!!!, %s, %s", compressionType, compressionType.getId()));
    }
}
