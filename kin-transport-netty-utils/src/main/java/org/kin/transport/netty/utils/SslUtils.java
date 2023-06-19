package org.kin.transport.netty.utils;

import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.FingerprintTrustManagerFactory;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.kin.framework.JvmCloseCleaner;
import org.kin.framework.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/6/18
 */
public final class SslUtils {
    private static final Logger log = LoggerFactory.getLogger(SslUtils.class);
    /** 支持的TLS协议 */
    private static final String[] PROTOCOLS = new String[]{"TLSv1.3", "TLSv1.2"};

    private SslUtils() {
    }

    /**
     * 获取ssl provider, 如果支持openssl, 则使用, 否则回退到使用jdk ssl
     */
    public static SslProvider getSslProvider() {
        if (OpenSsl.isAvailable()) {
            return SslProvider.OPENSSL_REFCNT;
        } else {
            return SslProvider.JDK;
        }
    }

    /**
     * 获取用于测试的自签名证书
     *
     * @return 自签名证书
     */
    private static SelfSignedCertificate getSelfSignedCertificate() throws CertificateException {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        //JVM exit时, 删除该证书
        JvmCloseCleaner.instance().add(ssc::delete);
        return ssc;
    }

    /**
     * set up server ssl context
     *
     * @param certFile        证书
     * @param certKeyFile     私钥文件
     * @param certKeyPassword 私钥文件密码
     * @param caFile          CA文件
     * @param fingerprintFile 指纹文件
     * @return ssl context
     */
    @Nonnull
    public static SslContext setUpServerSslContext(@Nullable File certFile,
                                                   @Nullable File certKeyFile,
                                                   @Nullable String certKeyPassword,
                                                   @Nullable File caFile,
                                                   @Nullable File fingerprintFile) {
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

            setUpTrustManager(sslContextBuilder, caFile, fingerprintFile, false);

            sslContextBuilder.protocols(PROTOCOLS)
                    .sslProvider(getSslProvider());
            return sslContextBuilder.build();
        } catch (CertificateException | SSLException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("unknown exception");
    }

    /**
     * set up server ssl context
     *
     * @param certFile        证书
     * @param certKeyFile     私钥文件
     * @param certKeyPassword 私钥文件密码
     * @param caFile          CA文件
     * @param fingerprintFile 指纹文件
     * @return ssl context
     */
    @Nonnull
    public static SslContext setUpClientSslContext(@Nullable File certFile,
                                                   @Nullable File certKeyFile,
                                                   @Nullable String certKeyPassword,
                                                   @Nullable File caFile,
                                                   @Nullable File fingerprintFile) {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                    .protocols(PROTOCOLS)
                    .sslProvider(getSslProvider());
            if (Objects.nonNull(certFile) && Objects.nonNull(certKeyFile)) {
                //配置证书和私钥
                sslContextBuilder = SslContextBuilder.forServer(certFile, certKeyFile, certKeyPassword);
            }

            setUpTrustManager(sslContextBuilder, caFile, fingerprintFile, true);

            return sslContextBuilder.build();
        } catch (SSLException e) {
            ExceptionUtils.throwExt(e);
        }

        throw new IllegalStateException("unknown exception");
    }

    /**
     * 添加信任证书
     * 优先级:
     * 1. ca file
     * 2. fingerprint file
     * 3. {@link InsecureTrustManagerFactory}
     */
    private static void setUpTrustManager(SslContextBuilder sslContextBuilder,
                                          @Nullable File caFile,
                                          @Nullable File fingerprintFile,
                                          boolean client) {
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
}
