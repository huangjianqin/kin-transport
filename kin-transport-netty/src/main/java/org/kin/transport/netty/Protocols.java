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

    /** 支持的最大数据内容长度(4Byte), {@link Integer#MAX_VALUE} */
    public static final int BODY_SIZE_MARK = 4;
    /** 支持的最大魔数bytes长度, 256B */
    public static final int MAX_MAGIC_SIZE = 256;
}
