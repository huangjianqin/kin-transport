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
    public final <MSG> WsServerTransportOption<MSG, BinaryWebSocketFrame> binaryServer(Class<MSG> msgClass) {
        return new WsServerTransportOption<>();
    }

    /** binary client配置 */
    public final <MSG> WsClientTransportOption<MSG, BinaryWebSocketFrame> binaryClient(Class<MSG> msgClass) {
        return new WsClientTransportOption<>();
    }

    /** text server配置 */
    public final <MSG> WsServerTransportOption<MSG, TextWebSocketFrame> textServer(Class<MSG> msgClass) {
        return new WsServerTransportOption<>();
    }

    /** text client配置 */
    public final <MSG> WsClientTransportOption<MSG, TextWebSocketFrame> textClient(Class<MSG> msgClass) {
        return new WsClientTransportOption<>();
    }
}
