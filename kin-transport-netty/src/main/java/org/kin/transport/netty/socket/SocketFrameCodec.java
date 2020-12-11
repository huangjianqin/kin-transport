package org.kin.transport.netty.socket;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * socket数据流解析
 * 主要是校验协议头, 并取出对应协议数据, 交给协议层去解析
 *
 * @author huangjianqin
 * @date 2019/5/29
 */
public class SocketFrameCodec extends ByteToMessageCodec<ByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(SocketFrameCodec.class);
    /** 魔数 */
    private static final byte[] FRAME_MAGIC_TOKEN = "kin-transport".getBytes();
    /** 协议帧长度字段占位大小 */
    private static final int FRAME_BODY_SIZE_TOKEN = 4;
    /** 协议头+协议体大小字段的字节长度 */
    private static final int FRAME_BASE_LENGTH = FRAME_MAGIC_TOKEN.length + FRAME_BODY_SIZE_TOKEN;
    /** 协议体数据部分最大长度 */
    private final int maxBodySize;
    /** true = server, false = client */
    private final boolean serverElseClient;

    //---------------------------------------------------------------------------------------------------------------

    /**
     * client端的数据流解析
     */
    public static SocketFrameCodec clientFrameCodec(int maxFrameSize) {
        return new SocketFrameCodec(maxFrameSize, false);
    }

    /**
     * client端的数据流解析
     */
    public static SocketFrameCodec clientFrameCodec() {
        //默认包体最大16M
        return clientFrameCodec(16 * 1024 * 1024);
    }

    /**
     * server端的数据流解析
     */
    public static SocketFrameCodec serverFrameCodec(int maxFrameSize) {
        return new SocketFrameCodec(maxFrameSize, true);
    }

    /**
     * server端的数据流解析
     */
    public static SocketFrameCodec serverFrameCodec() {
        //默认包体最大16M
        return serverFrameCodec(16 * 1024 * 1024);
    }

    //---------------------------------------------------------------------------------------------------------------

    /**
     * @param maxFrameSize     传输层帧大小
     * @param serverElseClient true = server, false = client
     */
    public SocketFrameCodec(int maxFrameSize, boolean serverElseClient) {
        this.maxBodySize = maxFrameSize - FRAME_BASE_LENGTH;
        Preconditions.checkArgument(this.maxBodySize > 0, String.format("maxFrameSize must be greater than %d bytes", FRAME_BASE_LENGTH));
        this.serverElseClient = serverElseClient;
    }

    /**
     * 判断魔数是否一致
     */
    private boolean isMagicRight(byte[] magic) {
        return Arrays.equals(magic, FRAME_MAGIC_TOKEN);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        try {
            if (!serverElseClient) {
                out.writeBytes(FRAME_MAGIC_TOKEN);
            }
            int bodySize = in.readableBytes();
            out.writeInt(bodySize);
            out.writeBytes(in, bodySize);
        } finally {
            ReferenceCountUtil.release(in);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            //解决拆包问题
            in.markReaderIndex();
            //合并解包
            int bodySize;
            if (serverElseClient) {
                if (in.readableBytes() < FRAME_BASE_LENGTH) {
                    return;
                }

                byte[] magic = new byte[FRAME_MAGIC_TOKEN.length];
                in.readBytes(magic);

                //校验魔数
                if (!isMagicRight(magic)) {
                    String hexDump = ByteBufUtil.hexDump(in);
                    //校验不通过, 直接清空inbound, 存在丢包的可能, 保证client存在重试机制
                    in.skipBytes(in.readableBytes());
                    throw new CorruptedFrameException(String.format("FrameHeaderError: magic=%s, HexDump=%s", Arrays.toString(magic), hexDump));
                }

                bodySize = in.readInt();

                if (bodySize > maxBodySize) {
                    //校验不通过, 直接清空inbound, 存在丢包的可能, 保证client存在重试机制
                    in.skipBytes(in.readableBytes());
                    throw new IllegalStateException(String.format("BodySize[%s] too large!", bodySize));
                }
            } else {
                if (in.readableBytes() < FRAME_BODY_SIZE_TOKEN) {
                    return;
                }

                bodySize = in.readInt();
            }

            int bodyReadableSize = in.readableBytes();
            if (bodyReadableSize < bodySize) {
                //解决拆包, 等待下次数据帧补满
                in.resetReaderIndex();
                return;
            }

            ByteBuf frameBuf = in.readRetainedSlice(bodySize);
            out.add(frameBuf);
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}
