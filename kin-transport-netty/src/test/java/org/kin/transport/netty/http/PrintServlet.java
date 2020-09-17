package org.kin.transport.netty.http;

import org.kin.transport.netty.http.server.AbstractServlet;
import org.kin.transport.netty.http.server.ServletRequest;
import org.kin.transport.netty.http.server.ServletResponse;

import java.util.Collections;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2020/9/17
 */
public class PrintServlet extends AbstractServlet {
    @Override
    protected Object doGet(ServletRequest request, ServletResponse response) {
        System.out.println(request.getContent());
        return "/abc";
    }

    @Override
    protected Object doPost(ServletRequest request, ServletResponse response, Map<String, Object> params) {
        System.out.println(params);
        return Collections.singletonMap("status", 1);
    }

}
