package org.kin.transport.netty;

/**
 * 协议相关常量
 *
 * @author huangjianqin
 * @date 2023/3/7
 */
public final class Protocols {
    private Protocols() {
    }

    /** 协议内容长度, 占4个字节 */
    public static final int PROTOCOL_LENGTH_MARK_BYTES = 4;
    /** 支持的最大数据内容长度, (2^30-1) */
    public static final int MAX_BODY_SIZE = 0x3fffffff;
    /** 支持的最大魔数bytes长度, 256B */
    public static final int MAX_MAGIC_SIZE = 256;
}
