package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.OpenSsl;
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
    protected static final String[] PROTOCOLS = new String[]{"TLSv1.3", "TLSv.1.2"};

    /** ssl */
    private boolean ssl;
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
    protected static void onServerSsl(SslProvider.SslContextSpec sslContextSpec,
                                      File certFile,
                                      File keyFile,
                                      String keyPassword) {
        try {
            SslContextBuilder sslContextBuilder;
            if (Objects.nonNull(certFile) && Objects.nonNull(keyFile)) {
                //配置证书和私钥
                sslContextBuilder = SslContextBuilder.forServer(certFile, keyFile, keyPassword);
            } else {
                //自签名证书, for test
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslContextBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
            }
            sslContextBuilder.protocols(PROTOCOLS)
                    .sslProvider(getSslProvider())
                    .clientAuth(ClientAuth.REQUIRE);
            sslContextSpec.sslContext(sslContextBuilder.build());
        } catch (SSLException | CertificateException e) {
            ExceptionUtils.throwExt(e);
        }
    }

    /**
     * 构建client端ssl上下文
     */
    protected static void onClientSsl(SslProvider.SslContextSpec sslContextSpec,
                                      File caFile) {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient()
                    .protocols(PROTOCOLS)
                    .sslProvider(getSslProvider());
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
    public boolean isSsl() {
        return ssl;
    }

    @SuppressWarnings("unchecked")
    public T ssl(boolean ssl) {
        this.ssl = ssl;
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
