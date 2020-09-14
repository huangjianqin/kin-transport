package org.kin.transport.netty.http;

/**
 * @author huangjianqin
 * @date 2020/9/14
 */
public class UnknownMediaTypeException extends RuntimeException {
    public UnknownMediaTypeException(String mediaType) {
        super(String.format("unknown media type '%s'", mediaType));
    }
}
