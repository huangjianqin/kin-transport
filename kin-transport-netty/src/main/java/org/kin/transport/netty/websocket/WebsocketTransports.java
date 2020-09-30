package org.kin.transport.netty.websocket;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.kin.transport.netty.websocket.client.WsClientTransportOption;
import org.kin.transport.netty.websocket.server.WsServerTransportOption;

/**
 * websocket transports
 *
 * @author huangjianqin
 * @date 2020/9/1
 */
public class WebsocketTransports {
    public static final WebsocketTransports INSTANCE = new WebsocketTransports();

    /** binary server配置 */
    public final <MSG> WsServerTransportOption.WsServerTransportOptionBuilder<MSG, BinaryWebSocketFrame> binaryServer(Class<MSG> msgClass) {
        return WsServerTransportOption.builder();
    }

    /** binary client配置 */
    public final <MSG> WsClientTransportOption.WsClientTransportOptionBuilder<MSG, BinaryWebSocketFrame> binaryClient(Class<MSG> msgClass) {
        return WsClientTransportOption.builder();
    }

    /** text server配置 */
    public final <MSG> WsServerTransportOption.WsServerTransportOptionBuilder<MSG, TextWebSocketFrame> textServer(Class<MSG> msgClass) {
        return WsServerTransportOption.builder();
    }

    /** text client配置 */
    public final <MSG> WsClientTransportOption.WsClientTransportOptionBuilder<MSG, TextWebSocketFrame> textClient(Class<MSG> msgClass) {
        return WsClientTransportOption.builder();
    }
}
