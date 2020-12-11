package org.kin.transport.netty.websocket;

import org.kin.transport.netty.ClientOptionOprs;

/**
 * websocket client options操作
 *
 * @author huangjianqin
 * @date 2020/12/11
 */
public interface WsClientOptionOprs<C> extends ClientOptionOprs<C> {
    /**
     * 根据url连接服务器
     */
    C connect(String url);
}
