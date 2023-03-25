package org.kin.transport.netty.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.kin.framework.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author huangjianqin
 * @date 2023/3/15
 */
public class ByteBufUtilsTest {
    public static void main(String[] args) {
        ByteBuf byteBuf = Unpooled.directBuffer();
        String s = StringUtils.randomString(ThreadLocalRandom.current().nextInt(30));
        byteBuf.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        System.out.println(s);
        System.out.println(byteBuf.readableBytes());
        printByteBuf(byteBuf);
        System.out.println("-------------------------------------");
        ByteBufUtils.rightShift(byteBuf, 10);
        System.out.println(byteBuf.readableBytes());
        printByteBuf(byteBuf);
        byteBuf.skipBytes(10);
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String ds = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(ds);
        System.out.println(ds.equals(s));
    }

    private static void printByteBuf(ByteBuf byteBuf) {
        int readerIndex = byteBuf.readerIndex();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        System.out.println(Arrays.toString(bytes));
        byteBuf.readerIndex(readerIndex);
    }
}