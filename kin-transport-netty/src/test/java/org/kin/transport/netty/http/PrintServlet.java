package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.AbstractServlet;
import org.kin.transport.netty.http.server.ServletRequest;
import org.kin.transport.netty.http.server.ServletResponse;

import java.util.Collections;

/**
 * @author huangjianqin
 * @date 2020/9/17
 */
public class PrintServlet extends AbstractServlet {
    @Override
    protected Object doGet(ServletRequest request, ServletResponse response) {
        System.out.println(request.getParams());
        return "test.html";
    }

    @Override
    protected Object doPost(ServletRequest request, ServletResponse response) {
        System.out.println(request.getParams());
        return Collections.singletonMap("status", 1);
    }

}
