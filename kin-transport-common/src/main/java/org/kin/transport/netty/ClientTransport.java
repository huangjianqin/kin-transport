package org.kin.transport.netty;

import io.netty.handler.ssl.SslContextBuilder;
import org.kin.framework.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/6/18
 */
public class ClientTransport<CT extends ClientTransport<CT>> extends Transport<CT> {
    private static final Logger log = LoggerFactory.getLogger(ClientTransport.class);

    /**
     * 自定义信任证书集合
     * null, 则表示使用系统默认
     * TLS握手时需要
     */
    private File caFile;

    /**
     * 检查是否配上必要配置
     */
    protected void checkRequire() {
        super.checkRequire();
        if (isSsl()) {
            if (Objects.isNull(caFile)) {
                log.warn("ssl is opened, but caFile is not set");
            }
        }
    }

    /**
     * 构建client端ssl上下文
     */
    protected void clientSSL(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                    .protocols(PROTOCOLS);
            if (Objects.nonNull(caFile)) {
                //配置握手信任证书
                sslContextBuilder.trustManager(caFile);
            }
            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException e) {
            ExceptionUtils.throwExt(e);
        }
    }


    //setter && getter
    public File getCaFile() {
        return caFile;
    }

    public CT caFile(String caFilePath) {
        return caFile(new File(caFilePath));
    }

    @SuppressWarnings("unchecked")
    public CT caFile(File caFile) {
        if (!caFile.exists()) {
            throw new IllegalArgumentException("caFile not exists");
        }
        this.caFile = caFile;
        return (CT) this;
    }
}
