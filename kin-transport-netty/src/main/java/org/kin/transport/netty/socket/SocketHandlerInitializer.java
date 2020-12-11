package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import org.kin.transport.netty.AbstractChannelHandlerInitializer;
import org.kin.transport.netty.socket.handler.SocketFrameCodec;
import org.kin.transport.netty.socket.protocol.SocketProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * socket channel handler初始化
 *
 * @author huangjianqin
 * @date 2019-09-12
 */
public class SocketHandlerInitializer
        extends AbstractChannelHandlerInitializer<ByteBuf, SocketProtocol, ByteBuf, SocketTransportOption> {
    private final boolean serverElseClient;

    public SocketHandlerInitializer(SocketTransportOption transportOption, boolean serverElseClient) {
        super(transportOption);
        this.serverElseClient = serverElseClient;
    }

    //---------------------------------------------------------------------------------------------------------------

    @Override
    protected Collection<ChannelHandler> firstHandlers() {
        List<ChannelHandler> channelHandlers = new ArrayList<>(super.firstHandlers());
        channelHandlers.add(new SocketFrameCodec(transportOption.getMaxFrameSize(), serverElseClient));
        //处理压缩
        channelHandlers.add(new SocketDecompressor());
        channelHandlers.add(new SocketCompressor(transportOption.getCompressionType()));
        MessageToByteEncoder<ByteBuf> compressionEncoder = transportOption.getCompressionType().encoder();
        if (Objects.nonNull(compressionEncoder)) {
            channelHandlers.add(compressionEncoder);
        }

        return channelHandlers;
    }
}
