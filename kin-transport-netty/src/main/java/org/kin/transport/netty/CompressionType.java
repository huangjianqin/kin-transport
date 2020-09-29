package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.compression.*;

import java.util.HashSet;

/**
 * 压缩方式
 * <p>
 * BZIP2和LZ4的netty实现基于buffer, buffer未满, 消息不可以flush出去, 不符合现在的协议设计, 暂不支持
 *
 * @author huangjianqin
 * @date 2020/9/28
 */
public enum CompressionType {
    /**
     * 不压缩
     */
    NONE(0) {
        @Override
        public ByteToMessageDecoder decoder() {
            return null;
        }

        @Override
        public MessageToByteEncoder<ByteBuf> encoder() {
            return null;
        }
    },
    /**
     * snappy
     */
    SNAPPY(1) {
        @Override
        public ByteToMessageDecoder decoder() {
            return new SnappyFrameDecoder();
        }

        @Override
        public MessageToByteEncoder<ByteBuf> encoder() {
            return new SnappyFrameEncoder();
        }
    },
    /**
     * zlib
     */
    ZLIB(3) {
        @Override
        public ByteToMessageDecoder decoder() {
            return new JdkZlibDecoder();
        }

        @Override
        public MessageToByteEncoder<ByteBuf> encoder() {
            return new JdkZlibEncoder();
        }
    },
    /**
     * lzf
     */
    LZF(4) {
        @Override
        public ByteToMessageDecoder decoder() {
            return new LzfDecoder();
        }

        @Override
        public MessageToByteEncoder<ByteBuf> encoder() {
            return new LzfEncoder();
        }
    },
    /**
     * fastLz
     */
    FASTLZ(6) {
        @Override
        public ByteToMessageDecoder decoder() {
            return new FastLzFrameDecoder();
        }

        @Override
        public MessageToByteEncoder<ByteBuf> encoder() {
            return new FastLzFrameEncoder();
        }
    },
    ;

    static {
        //加载时检查id是否存在重复
        HashSet<Integer> set = new HashSet<>();
        for (CompressionType type : values()) {
            if (!set.add(type.getId())) {
                throw new CompressionTypeIdConflictException(type);
            }
        }
    }

    private final int id;

    CompressionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * @return compressor
     */
    public abstract ByteToMessageDecoder decoder();

    /**
     * @return decompressor
     */
    public abstract MessageToByteEncoder<ByteBuf> encoder();

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * @return 根据名字获取压缩类型
     */
    public static CompressionType getByName(String typeName) {
        for (CompressionType type : values()) {
            if (type.name().toLowerCase().equals(typeName.toLowerCase())) {
                return type;
            }
        }

        throw new UnknownCompressionTypeException(typeName);
    }

    /**
     * @return 根据id获取压缩类型
     */
    public static CompressionType getByName(int id) {
        for (CompressionType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }
}
