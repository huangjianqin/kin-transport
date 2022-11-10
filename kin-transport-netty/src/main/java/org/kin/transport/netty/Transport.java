package org.kin.transport.netty;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.kin.framework.utils.ExceptionUtils;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * 传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport {
    /** ssl */
    protected boolean ssl;
    /** 证书路径 */
    protected File certFile;
    /** 证书密钥路径 */
    protected File certKeyFile;
    /**  */
    protected File caFile;

    /**
     * 检查是否配上必要配置
     */
    protected void checkRequire() {

    }

    /**
     * 构建ssl上下文
     */
    protected void secure(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder;
            if (Objects.nonNull(certFile) && Objects.nonNull(certKeyFile)) {
                sslContextBuilder = SslContextBuilder.forServer(certFile, certKeyFile);
                if (Objects.nonNull(caFile)) {
                    sslContextBuilder = sslContextBuilder.trustManager(certFile);
                }
            } else {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContextBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
            }
            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException | CertificateException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    //setter && getter
    public boolean isSsl() {
        return ssl;
    }

    public Transport ssl(boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public File getCertFile() {
        return certFile;
    }

    public Transport certFile(String certFilePath) {
        return certFile(new File(certFilePath));
    }

    public Transport certFile(File certFile) {
        if (!certFile.exists()) {
            throw new IllegalArgumentException("certFile not exists");
        }
        this.certFile = certFile;
        return this;
    }

    public File getCertKeyFile() {
        return certKeyFile;
    }

    public Transport certKeyFile(String certKeyFilePath) {
        return certKeyFile(new File(certKeyFilePath));
    }

    public Transport certKeyFile(File certKeyFile) {
        if (!certKeyFile.exists()) {
            throw new IllegalArgumentException("certKeyFile not exists");
        }
        this.certKeyFile = certKeyFile;
        return this;
    }

    public File getCaFile() {
        return caFile;
    }

    public Transport caFile(String caFilePath) {
        return caFile(new File(caFilePath));
    }

    public Transport caFile(File caFile) {
        if (!caFile.exists()) {
            throw new IllegalArgumentException("caFile not exists");
        }
        this.caFile = caFile;
        return this;
    }
}
