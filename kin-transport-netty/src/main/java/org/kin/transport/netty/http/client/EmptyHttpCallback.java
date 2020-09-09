package org.kin.transport.netty.http.client;

import java.io.IOException;

/**
 * @author huangjianqin
 * @date 2020/9/9
 */
public class EmptyHttpCallback implements HttpCallback {
    public static final EmptyHttpCallback INSTANCE = new EmptyHttpCallback();

    private EmptyHttpCallback() {
    }

    @Override
    public void onFailure(HttpCall httpCall, Exception exception) {
        //do nothing
    }

    @Override
    public void onResponse(HttpCall httpCall, HttpResponse httpResponse) throws IOException {
        //do nothing
    }
}
