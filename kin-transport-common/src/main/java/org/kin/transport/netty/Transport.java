package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
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
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport<T extends Transport<T>> {
    /** ssl */
    protected boolean ssl;
    /** 证书 */
    protected File certFile;
    /** 证书密钥 */
    protected File certKeyFile;
    /** CA根证书 */
    protected File caFile;
    /** 定义额外的netty child options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> childOptions = new HashMap<>();
    /** 定义额外的netty options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> options = new HashMap<>();

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
                    sslContextBuilder = sslContextBuilder.trustManager(caFile);
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

    /**
     * 应用child option
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <V extends reactor.netty.transport.ServerTransport<?, ?>> V applyChildOptions(V serverTransport) {
        for (Map.Entry<ChannelOption, Object> entry : getChildOptions().entrySet()) {
            serverTransport = (V) serverTransport.childOption(entry.getKey(), entry.getValue());
        }
        return serverTransport;
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

    public File getCertFile() {
        return certFile;
    }

    public T certFile(String certFilePath) {
        return certFile(new File(certFilePath));
    }

    @SuppressWarnings("unchecked")
    public T certFile(File certFile) {
        if (!certFile.exists()) {
            throw new IllegalArgumentException("certFile not exists");
        }
        this.certFile = certFile;
        return (T) this;
    }

    public File getCertKeyFile() {
        return certKeyFile;
    }

    public T certKeyFile(String certKeyFilePath) {
        return certKeyFile(new File(certKeyFilePath));
    }

    @SuppressWarnings("unchecked")
    public T certKeyFile(File certKeyFile) {
        if (!certKeyFile.exists()) {
            throw new IllegalArgumentException("certKeyFile not exists");
        }
        this.certKeyFile = certKeyFile;
        return (T) this;
    }

    public File getCaFile() {
        return caFile;
    }

    public T caFile(String caFilePath) {
        return caFile(new File(caFilePath));
    }

    @SuppressWarnings("unchecked")
    public T caFile(File caFile) {
        if (!caFile.exists()) {
            throw new IllegalArgumentException("caFile not exists");
        }
        this.caFile = caFile;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <A> T childOption(ChannelOption<A> option, A value) {
        childOptions.put(option, value);
        return (T) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public T childOption(Map<ChannelOption, Object> childOptions) {
        this.childOptions.putAll(childOptions);
        return (T) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getChildOptions() {
        return childOptions;
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
