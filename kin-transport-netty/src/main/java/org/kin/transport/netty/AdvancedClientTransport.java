package org.kin.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.tcp.SslProvider;

import java.io.File;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/3/28
 */
public abstract class AdvancedClientTransport<ACT extends AdvancedClientTransport<ACT>> extends AdvancedTransport<ACT> {
    private static final Logger log = LoggerFactory.getLogger(AdvancedClientTransport.class);

    @SuppressWarnings("rawtypes")
    private ClientObserver observer = ClientObserver.DEFAULT;
    /**
     * 信任证书集合
     * 信任证书即私钥提交CA签名后的证书, 用于校验server端证书权限
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
    protected void clientSsl(SslProvider.SslContextSpec sslContextSpec) {
        onClientSsl(sslContextSpec, caFile);
    }

    //setter && getter
    @SuppressWarnings("rawtypes")
    public ClientObserver getObserver() {
        return observer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ACT observer(ClientObserver observer) {
        this.observer = observer;
        return (ACT) this;
    }

    public File getCaFile() {
        return caFile;
    }

    public ACT caFile(String caFilePath) {
        return caFile(new File(caFilePath));
    }

    @SuppressWarnings("unchecked")
    public ACT caFile(File caFile) {
        if (!caFile.exists()) {
            throw new IllegalArgumentException("caFile not exists");
        }
        this.caFile = caFile;
        return (ACT) this;
    }
}
