package org.kin.transport.netty;

/**
 * connection 抽象
 *
 * @author 健勤
 * @date 2017/2/10
 */
public abstract class AbstractConnection {
    protected final AbstractTransportOption transportOption;
    protected final ChannelHandlerInitializer channelHandlerInitializer;

    public AbstractConnection(AbstractTransportOption transportOption, ChannelHandlerInitializer channelHandlerInitializer) {
        this.transportOption = transportOption;
        this.channelHandlerInitializer = channelHandlerInitializer;
    }

    /**
     * 连接关闭
     */
    public abstract void close();

    /**
     * @return 绑定地址 or 远程服务器地址
     */
    public abstract String getAddress();

    /**
     * 检查连接是否有效
     *
     * @return 连接是否有效
     */
    public abstract boolean isActive();
}
