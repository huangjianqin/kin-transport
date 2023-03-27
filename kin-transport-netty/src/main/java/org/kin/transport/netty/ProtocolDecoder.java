package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * 协议解析
 * <p>
 * 消息头组成:
 * 协议内容长度, int, 占4个字节
 * magic, bytes, 使用者配置而定
 * 数据内容
 *
 * @author huangjianqin
 * @date 2023/1/15
 */
public class ProtocolDecoder extends ReplayingDecoder<ProtocolDecoder.State> {
    /** 协议配置 */
    private final ProtocolOptions options;
    /** 目前解析到的协议头信息 */
    private final ProtocolHeader header;

    public ProtocolDecoder(ProtocolOptions options) {
        super(State.LENGTH_FIELD);
        this.options = options;
        this.header = new ProtocolHeader(this.options.getMagicSize());
        if (options.isUseCompositeBuf()) {
            setCumulator(COMPOSITE_CUMULATOR);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) {
        //尝试一次读取多个协议
        while (true) {
            //读取中发生异常, 则会自动回滚到上一次的checkpoint, 不用自己写try-catch和回滚逻辑
            //缺点, 每个channel一个实例
            State state = state();
            switch (state) {
                case LENGTH_FIELD:
                    header.length(byteBuf.readInt());
                    checkpoint(State.MAGIC);
                case MAGIC:
                    byteBuf.readBytes(header.getMagicBytes());
                    isMagicMatch(header.getMagicBytes());
                    checkpoint(State.BODY);
                case BODY:
                    int bodySize = checkBodySize(header.getBodySize());
                    ByteBuf bodyByteBuf = byteBuf.readRetainedSlice(bodySize);
                    //reactor netty会对inbound obj进行release, 所以这里有必要retain一下
                    out.add(ByteBufPayload.create(bodyByteBuf).retain());
                    checkpoint(State.LENGTH_FIELD);
            }
        }
    }

    /**
     * 检查协议魔数与配置魔数是否一致
     *
     * @param magicBytes 协议魔数bytes
     */
    private void isMagicMatch(byte[] magicBytes) {
        if (!Arrays.equals(options.getMagic(), magicBytes)) {
            throw new TransportException("magic is not match");
        }
    }

    /**
     * 检查数据内容大小是否大于配置的最大大小
     *
     * @param size 协议内容大小
     * @return 协议内容大小
     */
    private int checkBodySize(int size) {
        if (size > options.getMaxBodySize()) {
            throw new TransportException(String.format("actual protocol content length(%d) is bigger than max body size(%d)", size, options.getMaxBodySize()));
        }
        return size;
    }

    /**
     * 协议解析的不同阶段
     */
    enum State {
        /** 解析协议内容长度 */
        LENGTH_FIELD,
        /** 解析魔数 */
        MAGIC,
        /** 解析数据内容 */
        BODY
    }
}
