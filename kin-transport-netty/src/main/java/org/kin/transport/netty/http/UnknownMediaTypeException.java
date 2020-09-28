package org.kin.transport.netty.http;

/**
 * 未知 MediaType 异常
 *
 * @author huangjianqin
 * @date 2020/9/14
 */
public class UnknownMediaTypeException extends RuntimeException {
    private static final long serialVersionUID = 4416081422231678494L;

    public UnknownMediaTypeException(String mediaType) {
        super(String.format("unknown media type '%s'", mediaType));
    }
}
