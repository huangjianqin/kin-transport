package org.kin.transport.netty;

import org.kin.transport.netty.core.domain.Request;
import org.kin.transport.netty.core.domain.Response;
import org.kin.transport.netty.core.protocol.AbstractProtocol;
import org.kin.transport.netty.core.protocol.Protocol;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
@Protocol(id = 1)
public class Protocol1 extends AbstractProtocol {
    private int f;

    @Override
    public void read(Request request) {
        f = request.readInt();
    }

    @Override
    public void write(Response response) {
        response.writeInt(f);
    }

    @Override
    public String toString() {
        return super.toString() + "Protocol1{" +
                "f=" + f +
                '}';
    }
}