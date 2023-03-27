package org.kin.transport.netty;

import org.kin.framework.log.LoggerOprs;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;

import java.util.List;
import java.util.Objects;

/**
 * server抽象, 统一payload处理流程
 *
 * @author huangjianqin
 * @date 2023/1/20
 */
public abstract class Server<PT extends AbsLengthFieldBaseTransport<PT>> implements Disposable, LoggerOprs {
    /** server配置 */
    protected final PT serverTransport;
    /** 监听端口 */
    protected final int port;
    /** server disposable */
    protected volatile DisposableServer disposable;

    protected Server(PT serverTransport, int port) {
        this.serverTransport = serverTransport;
        this.port = port;
    }

    /**
     * @return server命名
     */
    protected String serverName() {
        return getClass().getSimpleName();
    }

    /**
     * client连接成功逻辑处理
     */
    @SuppressWarnings({"unchecked"})
    protected void onClientConnected(Session session) {
        PayloadProcessor payloadProcessor = serverTransport.getPayloadProcessor();
        session.connection()
                .inbound()
                .receiveObject()
                .flatMap(o -> {
                    //有可能一次多个payload
                    if (o instanceof List) {
                        return Flux.fromIterable(((List<Object>) o));
                    } else {
                        return Flux.just(o);
                    }
                })
                .filter(o -> {
                    //过滤非法payload
                    if (!(o instanceof ByteBufPayload)) {
                        log().warn("unexpected payload type received: {}, channel: {}.", o.getClass(), session.channel());
                        return false;
                    }

                    return true;
                })
                .cast(ByteBufPayload.class)
                .flatMap(bp -> payloadProcessor.process(session, bp))
                .onErrorContinue((throwable, o) -> {
                    log().error("{} process payload error, {}\r\n{}", serverName(), o, throwable);
                })
                .subscribe();
    }

    @Override
    public void dispose() {
        if (Objects.isNull(disposable) || disposable.isDisposed()) {
            return;
        }

        disposable.dispose();
    }
}
