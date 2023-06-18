package org.kin.transport.netty;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.tcp.SslProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author huangjianqin
 * @date 2023/4/1
 */
public abstract class ServerTransport<ST extends ServerTransport<ST>> extends Transport<ST> {
    private static final Logger log = LoggerFactory.getLogger(ServerTransport.class);

    /** 定义额外的netty child options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> childOptions = new HashMap<>();
    /**
     * certificate chain file
     * 证书链文件, 所谓链, 即custom certificate -> root certificate
     */
    private File certFile;
    /** private key file */
    private File certKeyFile;
    /** the password of the {@code keyFile}, or {@code null} if it's not password-protected */
    private String certKeyPassword;

    /**
     * 检查是否配上必要配置
     */
    protected void checkRequire() {
        super.checkRequire();
        if (isSsl()) {
            if (Objects.isNull(certFile)) {
                log.warn("ssl is opened, but certFile is not set");
            }

            if (Objects.isNull(certKeyFile)) {
                log.warn("ssl is opened, but certKeyFile is not set");
            }
        }
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

    /**
     * 构建server端ssl上下文
     */
    protected void serverSsl(SslProvider.SslContextSpec sslContextSpec) {
        onServerSsl(sslContextSpec, certFile, certKeyFile, certKeyPassword);
    }

    //setter && getter
    @SuppressWarnings("unchecked")
    public <A> ST childOption(ChannelOption<A> option, A value) {
        childOptions.put(option, value);
        return (ST) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ST childOption(Map<ChannelOption, Object> childOptions) {
        this.childOptions.putAll(childOptions);
        return (ST) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getChildOptions() {
        return childOptions;
    }

    public File getCertFile() {
        return certFile;
    }

    public ST certFile(String certFilePath) {
        return certFile(new File(certFilePath));
    }

    @SuppressWarnings("unchecked")
    public ST certFile(File certFile) {
        if (!certFile.exists()) {
            throw new IllegalArgumentException("certFile not exists");
        }
        this.certFile = certFile;
        return (ST) this;
    }

    public File getCertKeyFile() {
        return certKeyFile;
    }

    public ST certKeyFile(String certKeyFilePath) {
        return certKeyFile(new File(certKeyFilePath));
    }

    @SuppressWarnings("unchecked")
    public ST certKeyFile(File certKeyFile) {
        if (!certKeyFile.exists()) {
            throw new IllegalArgumentException("certKeyFile not exists");
        }
        this.certKeyFile = certKeyFile;
        return (ST) this;
    }

    public String getCertKeyPassword() {
        return certKeyPassword;
    }

    @SuppressWarnings("unchecked")
    public ST certKeyPassword(String certKeyPassword) {
        this.certKeyPassword = certKeyPassword;
        return (ST) this;
    }
}
