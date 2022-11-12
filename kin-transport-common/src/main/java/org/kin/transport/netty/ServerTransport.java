package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import org.kin.framework.utils.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 服务端传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class ServerTransport extends Transport {
    /** 自定义http server transport配置 */
    private final Set<ServerTransportCustomizer> serverTransportCustomizers = new HashSet<>();

    protected <ST extends reactor.netty.transport.ServerTransport<?, ?>> ST customServerTransport(ST serverTransport) {
        if (CollectionUtils.isNonEmpty(serverTransportCustomizers)) {
            //外部自定义reactor netty server transport
            for (ServerTransportCustomizer customizer : serverTransportCustomizers) {
                serverTransport = customizer.custom(serverTransport);
            }
        }

        return serverTransport;
    }

    //setter && getter

    /**
     * 自定义http server transport配置
     */
    @SuppressWarnings("unchecked")
    public <ST extends ServerTransport> ST serverTransportCustomizer(ServerTransportCustomizer customizer) {
        Preconditions.checkNotNull(customizer);
        serverTransportCustomizers.add(customizer);
        return (ST) this;
    }

    public Set<ServerTransportCustomizer> getServerTransportCustomizers() {
        return serverTransportCustomizers;
    }
}
