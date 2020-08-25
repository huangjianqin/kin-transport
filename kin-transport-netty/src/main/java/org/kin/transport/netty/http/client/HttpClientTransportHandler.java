package org.kin.transport.netty.http.client;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import org.kin.transport.netty.TransportHandler;

/**
 * @author huangjianqin
 * @date 2020/8/25
 */
public abstract class HttpClientTransportHandler extends TransportHandler<FullHttpResponse> {
    @Override
    public final void channelActive(Channel channel) {
        super.channelActive(channel);
        //do nothing
    }

    @Override
    public final void channelInactive(Channel channel) {
        super.channelInactive(channel);
        //do nothing
    }

    @Override
    public final void readWriteIdle(Channel channel) {
        super.readWriteIdle(channel);
        //do nothing
    }

    @Override
    public final void readIdle(Channel channel) {
        super.readIdle(channel);
        //do nothing
    }

    @Override
    public final void writeIdel(Channel channel) {
        super.writeIdel(channel);
        //do nothing
    }


}
