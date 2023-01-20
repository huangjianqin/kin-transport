package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;

/**
 * @author huangjianqin
 * @date 2023/1/15
 */
public class ByteBufPayload {
    /** request bytebuf */
    protected final ByteBuf data;

    public ByteBufPayload(ByteBuf data) {
        this.data = data;
    }

    //getter
    public ByteBuf getData() {
        return data;
    }
}
