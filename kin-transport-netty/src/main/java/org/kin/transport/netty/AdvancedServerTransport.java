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
 * @date 2023/3/28
 */
public abstract class AdvancedServerTransport<AST extends AdvancedServerTransport<AST>> extends AdvancedTransport<AST> {
    private static final Logger log = LoggerFactory.getLogger(AdvancedServerTransport.class);

    @SuppressWarnings("rawtypes")
    private ServerObserver observer = ServerObserver.DEFAULT;
    /** 定义额外的netty child options */
    @SuppressWarnings("rawtypes")
    private final Map<ChannelOption, Object> childOptions = new HashMap<>();
    /**
     * certificate chain file
     * 证书链文件, 所谓链, 即custom certificate -> root certificate
     */
    private File certFile;
    /** private key file */
    private File keyFile;
    /** the password of the {@code keyFile}, or {@code null} if it's not password-protected */
    private String keyPassword;

    /**
     * 检查是否配上必要配置
     */
    protected void checkRequire() {
        super.checkRequire();
        if (isSsl()) {
            if (Objects.isNull(certFile)) {
                log.warn("ssl is opened, but certFile is not set");
            }

            if (Objects.isNull(keyFile)) {
                log.warn("ssl is opened, but keyFile is not set");
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
        onServerSsl(sslContextSpec, certFile, keyFile, keyPassword);
    }

    //setter && getter
    @SuppressWarnings("unchecked")
    public <A> AST childOption(ChannelOption<A> option, A value) {
        childOptions.put(option, value);
        return (AST) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AST childOption(Map<ChannelOption, Object> childOptions) {
        this.childOptions.putAll(childOptions);
        return (AST) this;
    }

    @SuppressWarnings("rawtypes")
    public Map<ChannelOption, Object> getChildOptions() {
        return childOptions;
    }

    @SuppressWarnings("rawtypes")
    public ServerObserver getObserver() {
        return observer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public AST observer(ServerObserver observer) {
        this.observer = observer;
        return (AST) this;
    }

    public File getCertFile() {
        return certFile;
    }

    public AST certFile(String certFilePath) {
        return certFile(new File(certFilePath));
    }

    @SuppressWarnings("unchecked")
    public AST certFile(File certFile) {
        if (!certFile.exists()) {
            throw new IllegalArgumentException("certFile not exists");
        }
        this.certFile = certFile;
        return (AST) this;
    }

    public File getKeyFile() {
        return keyFile;
    }

    public AST keyFile(String keyFilePath) {
        return keyFile(new File(keyFilePath));
    }

    @SuppressWarnings("unchecked")
    public AST keyFile(File keyFile) {
        if (!keyFile.exists()) {
            throw new IllegalArgumentException("keyFile not exists");
        }
        this.keyFile = keyFile;
        return (AST) this;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @SuppressWarnings("unchecked")
    public AST keyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
        return (AST) this;
    }
}
