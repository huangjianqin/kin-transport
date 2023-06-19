package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
import org.kin.transport.netty.utils.SslUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.tcp.SslProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport<T extends Transport<T>> {
    private static final Logger log = LoggerFactory.getLogger(Transport.class);

    //--------------------------------------------ssl配置 start
    //--------------------------------------------cert和ca都配置了, 标识开启双向认证
    /** ssl */
    private boolean ssl;
    //--------------------------------------------server ssl配置
    /**
     * certificate chain file
     * 证书链文件, 所谓链, 即custom certificate -> root certificate
     */
    private File certFile;
    /** private key file */
    private File certKeyFile;
    /** the password of the {@code keyFile}, or {@code null} if it's not password-protected */
    private String certKeyPassword;
    //--------------------------------------------client ssl配置
    /**
     * 自定义信任证书集合
     * 信任证书即私钥提交CA签名后的证书, 用于校验server端证书权限
     * null, 则表示使用系统默认
     * TLS握手时需要
     */
    private File caFile;
    /** 证书指纹 */
    private File fingerprintFile;
    //--------------------------------------------ssl配置 end
    /** 自定义netty options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> options = new HashMap<>();

    /**
     * 检查配置合法性
     */
    protected void checkRequire() {
        //default do nothing
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
     * 构建server端ssl上下文
     */
    protected void serverSsl(SslProvider.SslContextSpec sslContextSpec) {
        sslContextSpec.sslContext(
                SslUtils.setUpServerSslContext(
                        getCertFile(),
                        getCertKeyFile(),
                        getCertKeyPassword(),
                        getCaFile(),
                        getFingerprintFile()));
    }

    /**
     * 构建client端ssl上下文
     */
    protected void clientSsl(SslProvider.SslContextSpec sslContextSpec) {
        sslContextSpec.sslContext(
                SslUtils.setUpClientSslContext(
                        getCertFile(),
                        getCertKeyFile(),
                        getCertKeyPassword(),
                        getCaFile(),
                        getFingerprintFile()));
    }

    //setter && getter
    public boolean isSsl() {
        return ssl;
    }

    @SuppressWarnings("unchecked")
    public T ssl() {
        this.ssl = true;
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

    public String getCertKeyPassword() {
        return certKeyPassword;
    }

    @SuppressWarnings("unchecked")
    public T certKeyPassword(String certKeyPassword) {
        this.certKeyPassword = certKeyPassword;
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

    public File getFingerprintFile() {
        return fingerprintFile;
    }

    public Transport<T> fingerprintFile(File fingerprintFile) {
        if (!fingerprintFile.exists()) {
            throw new IllegalArgumentException("fingerprintFile not exists");
        }
        this.fingerprintFile = fingerprintFile;
        return this;
    }
}
