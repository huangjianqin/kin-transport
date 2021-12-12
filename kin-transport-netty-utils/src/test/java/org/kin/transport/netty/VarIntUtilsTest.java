package org.kin.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.transport.netty.utils.VarIntUtils;

/**
 * @author huangjianqin
 * @date 2021/12/12
 */
public class VarIntUtilsTest {
    public static void main(String[] args) {

        int a = Integer.MIN_VALUE;
        int b = Integer.MIN_VALUE / 2;
        int c = 0;
        int d = Integer.MAX_VALUE / 2;
        int e = Integer.MAX_VALUE;
        ByteBuf byteBuf1 = Unpooled.buffer();
        VarIntUtils.writeRawVarInt32(byteBuf1, a);
        VarIntUtils.writeRawVarInt32(byteBuf1, b);
        VarIntUtils.writeRawVarInt32(byteBuf1, c);
        VarIntUtils.writeRawVarInt32(byteBuf1, d);
        VarIntUtils.writeRawVarInt32(byteBuf1, e);

        System.out.println(VarIntUtils.readRawVarInt32(byteBuf1));
        System.out.println(VarIntUtils.readRawVarInt32(byteBuf1));
        System.out.println(VarIntUtils.readRawVarInt32(byteBuf1));
        System.out.println(VarIntUtils.readRawVarInt32(byteBuf1));
        System.out.println(VarIntUtils.readRawVarInt32(byteBuf1));

        System.out.println("-------------------------------------------");

        long a1 = Long.MIN_VALUE;
        long b1 = Long.MIN_VALUE / 2;
        long c1 = 0;
        long d1 = Long.MAX_VALUE / 2;
        long e1 = Long.MAX_VALUE;
        ByteBuf byteBuf2 = Unpooled.buffer();
        VarIntUtils.writeRawVarLong64(byteBuf2, a1);
        VarIntUtils.writeRawVarLong64(byteBuf2, b1);
        VarIntUtils.writeRawVarLong64(byteBuf2, c1);
        VarIntUtils.writeRawVarLong64(byteBuf2, d1);
        VarIntUtils.writeRawVarLong64(byteBuf2, e1);

        System.out.println(VarIntUtils.readRawVarLong64(byteBuf2));
        System.out.println(VarIntUtils.readRawVarLong64(byteBuf2));
        System.out.println(VarIntUtils.readRawVarLong64(byteBuf2));
        System.out.println(VarIntUtils.readRawVarLong64(byteBuf2));
        System.out.println(VarIntUtils.readRawVarLong64(byteBuf2));
    }
}
