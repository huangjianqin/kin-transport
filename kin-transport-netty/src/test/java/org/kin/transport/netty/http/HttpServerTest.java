package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.KinHttpServer;

import java.net.InetSocketAddress;

/**
 * @author huangjianqin
 * @date 2020/9/1
 */
public class HttpServerTest {
    public static void main(String[] args) throws InterruptedException {
        InetSocketAddress address = new InetSocketAddress(8880);
        KinHttpServer.builder().mappingServlet("/", PrintServlet.class).build(address);
    }
}
