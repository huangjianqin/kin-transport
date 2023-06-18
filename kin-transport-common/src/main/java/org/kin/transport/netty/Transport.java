package org.kin.transport.netty;

import com.google.common.base.Preconditions;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.kin.framework.utils.ExceptionUtils;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 传输层通用配置
 * <p>
 * 对于{@link #keyFile}
 * server: 私钥
 * client: 公钥
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport<T extends Transport<T>> {
    /** ssl */
    private boolean ssl;
    /** certificate chain file */
    private File keyCertChainFile;
    /** private key file */
    private File keyFile;
    /** the password of the {@code keyFile}, or {@code null} if it's not password-protected */
    private String keyPassword;
    /**
     * 自定义信任证书集合
     * null, 则表示使用系统默认
     * TLS握手时需要
     */
    private File trustCertCollectionFile;
    /** 定义额外的netty options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> options = new HashMap<>();

    /**
     * 检查是否配上必要配置
     */
    protected void checkRequire() {
        if (ssl) {
            Preconditions.checkNotNull(keyCertChainFile, "certFile must be not blank if open ssl");
            Preconditions.checkNotNull(keyFile, "certKeyFile must be not blank if open ssl");
        }
    }

    /**
     * 构建server端ssl上下文
     */
    protected void serverSSL(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder;
            if (Objects.nonNull(keyCertChainFile) && Objects.nonNull(keyFile)) {
                //配置key
                sslContextBuilder = SslContextBuilder.forServer(keyCertChainFile, keyFile, keyPassword);
                if (Objects.nonNull(trustCertCollectionFile)) {
                    //配置握手信任证书
                    sslContextBuilder = sslContextBuilder.trustManager(trustCertCollectionFile)
                            .clientAuth(ClientAuth.REQUIRE);
                }
            } else {
                //自签名证书
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContextBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
            }
            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException | CertificateException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 构建client端ssl上下文
     */
    protected void clientSSL(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            if (Objects.nonNull(trustCertCollectionFile)) {
                //配置握手信任证书
                sslContextBuilder.trustManager(trustCertCollectionFile);
            }
            if (Objects.nonNull(keyCertChainFile) && Objects.nonNull(keyFile)) {
                //配置key
                sslContextBuilder.keyManager(keyCertChainFile, keyFile, keyPassword);
            }

            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 应用option
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <V extends reactor.netty.transport.Transport<?, ?>> V applyOptions(V transport) {
        for (Map.Entry<ChannelOption, Object> entry : getOptions().entrySet()) {
            transport = (V) transport.option(entry.getKey(), entry.getValue());
        }
        return transport;
    }

    //setter && getter
    public boolean isSsl() {
        return ssl;
    }

    @SuppressWarnings("unchecked")
    public T ssl(boolean ssl) {
        this.ssl = ssl;
        return (T) this;
    }

    public File getKeyCertChainFile() {
        return keyCertChainFile;
    }

    public T keyCertChainFile(String keyCertChainFilePath) {
        return keyCertChainFile(new File(keyCertChainFilePath));
    }

    @SuppressWarnings("unchecked")
    public T keyCertChainFile(File keyCertChainFile) {
        if (!keyCertChainFile.exists()) {
            throw new IllegalArgumentException("keyCertChainFile not exists");
        }
        this.keyCertChainFile = keyCertChainFile;
        return (T) this;
    }

    public File getKeyFile() {
        return keyFile;
    }

    public T keyFile(String keyFilePath) {
        return keyFile(new File(keyFilePath));
    }

    @SuppressWarnings("unchecked")
    public T keyFile(File keyFile) {
        if (!keyFile.exists()) {
            throw new IllegalArgumentException("keyFile not exists");
        }
        this.keyFile = keyFile;
        return (T) this;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @SuppressWarnings("unchecked")
    public T keyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
        return (T) this;
    }

    public File getTrustCertCollectionFile() {
        return trustCertCollectionFile;
    }

    public T trustCertCollectionFile(String trustCertCollectionFilePath) {
        return trustCertCollectionFile(new File(trustCertCollectionFilePath));
    }

    @SuppressWarnings("unchecked")
    public T trustCertCollectionFile(File trustCertCollectionFile) {
        if (!trustCertCollectionFile.exists()) {
            throw new IllegalArgumentException("trustCertCollectionFile not exists");
        }
        this.trustCertCollectionFile = trustCertCollectionFile;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <A> T option(ChannelOption<A> option, A value) {
        options.put(option, value);
        return (T) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public T option(Map<ChannelOption, Object> options) {
        this.options.putAll(options);
        return (T) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getOptions() {
        return options;
    }
}
