package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.FingerprintTrustManagerFactory;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.tcp.SslProvider;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * 传输层通用配置
 *
 * @author huangjianqin
 * @date 2022/11/10
 */
public abstract class Transport<T extends Transport<T>> {
    private static final Logger log = LoggerFactory.getLogger(Transport.class);

    protected static final String[] PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2"};

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
     * lazy获取用于测试自签名证书
     *
     * @return 自签名证书
     */
    @Nonnull
    private static synchronized SelfSignedCertificate getSelfSignedCertificate() throws CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        //JVM exit时, 删除该证书
        JvmCloseCleaner.instance().add(ssc::delete);
        return ssc;
    }

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

    //--------------------------------------------------static

    /**
     * 获取ssl provider, 如果支持openssl, 则使用, 否则回退到使用jdk ssl
     */
    protected static io.netty.handler.ssl.SslProvider getSslProvider() {
        if (OpenSsl.isAvailable()) {
            return io.netty.handler.ssl.SslProvider.OPENSSL_REFCNT;
        } else {
            return io.netty.handler.ssl.SslProvider.JDK;
        }
    }

    /**
     * 构建server端ssl上下文
     */
    protected void serverSsl(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder;
            if (Objects.nonNull(certFile) && Objects.nonNull(certKeyFile)) {
                //配置证书和私钥
                sslContextBuilder = SslContextBuilder.forServer(certFile, certKeyFile, certKeyPassword)
                        .clientAuth(ClientAuth.REQUIRE);
            } else {
                log.warn("server ssl is opened, but certFile or certKeyFile is not set, just use internal self signed certificate for test");

                //自签名证书, for test
                SelfSignedCertificate ssc = getSelfSignedCertificate();
                sslContextBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                        .clientAuth(ClientAuth.OPTIONAL);
            }

            setUpTrustManager(sslContextBuilder, false);

            sslContextBuilder.protocols(PROTOCOLS)
                    .sslProvider(getSslProvider());
            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException | CertificateException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 构建client端ssl上下文
     */
    protected void clientSsl(SslProvider.SslContextSpec sslContextSpec) {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                    .protocols(PROTOCOLS)
                    .sslProvider(getSslProvider());
            if (Objects.nonNull(certFile) && Objects.nonNull(certKeyFile)) {
                //配置证书和私钥
                sslContextBuilder = SslContextBuilder.forServer(certFile, certKeyFile, certKeyPassword);
            }

            setUpTrustManager(sslContextBuilder, true);

            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 设置信任证书
     * 优先级:
     * 1. ca file
     * 2. fingerprint file
     * 3. {@link InsecureTrustManagerFactory}
     */
    private void setUpTrustManager(SslContextBuilder sslContextBuilder, boolean client) {
        if (Objects.nonNull(caFile)) {
            sslContextBuilder.trustManager(caFile);
        } else if (Objects.nonNull(fingerprintFile)) {
            //指纹适用于不能联网或者自签名环境, 同样安全
            List<String> fingerprintSha256List = new ArrayList<>();
            //读取指纹
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(fingerprintFile.toPath()), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        //一行一串SHA-256 hashed fingerprint
                        fingerprintSha256List.add(line.trim());
                    }
                }
            } catch (Exception ignore) {
                //do nothing
            }

            if (!fingerprintSha256List.isEmpty()) {
                //构建trust manager
                sslContextBuilder.trustManager(FingerprintTrustManagerFactory.builder("SHA-256")
                        .fingerprints(fingerprintSha256List)
                        .build());
            }
        } else {
            if (client) {
                log.warn("client ssl is opened, but caFile or fingerPrintFile is not set, just accept any certificate");
            }
            //默认接受任何证书, 忽略所有证书校验异常
            sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }
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
