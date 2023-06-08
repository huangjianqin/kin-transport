package org.kin.transport.netty;

import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

import java.util.List;
import java.util.Objects;

/**
 * server抽象, 统一payload处理流程
 *
 * @author huangjianqin
 * @date 2023/1/20
 */
public abstract class Server<PT extends ProtocolTransport<PT>> implements Disposable {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    /** server配置 */
    protected final PT serverTransport;
    /** 监听端口 */
    protected final int port;
    /** server disposable */
    private volatile DisposableServer disposable;

    protected Server(PT serverTransport, int port) {
        this.serverTransport = serverTransport;
        this.port = port;
    }

    /**
     * @return server命名
     */
    protected final String serverName() {
        return getClass().getSimpleName();
    }

    /**
     * client连接成功逻辑处理
     */
    @SuppressWarnings({"unchecked"})
    protected final void onClientConnected(Session session) {
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
                        log.warn("unexpected payload type received: {}, channel: {}.", o.getClass(), session.channel());
                        return false;
                    }

                    return true;
                })
                .cast(ByteBufPayload.class)
                .flatMap(bp -> {
                    try {
                        return payloadProcessor.process(session, bp).thenReturn(bp);
                    } finally {
                        ReferenceCountUtil.safeRelease(bp);
                    }
                })
                .onErrorContinue((throwable, o) -> {
                    log.error("{} process payload error, {}", serverName(), o, throwable);
                })
                .subscribe();
    }

    /**
     * process after server bound
     */
    protected final void onBound(DisposableServer disposable) {
        this.disposable = disposable;
    }

    @Override
    public final void dispose() {
        if (Objects.isNull(disposable) || disposable.isDisposed()) {
            return;
        }

        disposable.dispose();
    }

    /**
     * 返回disposed signal mono
     *
     * @return disposed signal mono
     */
    public final Mono<Void> onDispose() {
        if (Objects.isNull(disposable)) {
            return Mono.empty();
        }

        return disposable.onDispose();
    }
}
