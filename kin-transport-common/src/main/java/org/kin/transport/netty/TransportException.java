package org.kin.transport.netty;

/**
 * 传输层异常
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public class TransportException extends RuntimeException {
    private static final long serialVersionUID = 8263040649235604032L;

    public TransportException(String message) {
        super(message);
    }

    public TransportException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransportException(Throwable cause) {
        super(cause);
    }

    public TransportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TransportException() {
    }
}
