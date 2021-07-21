package org.kin.transport.netty.socket;

import io.netty.buffer.ByteBuf;
import org.kin.transport.netty.socket.protocol.*;

import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/7/21
 */
public class ProtostuffSocketTransferTest {
    public static void main(String[] args) throws Exception {
        ProtocolFactory.init("org.kin.transport");
        ProtostuffSocketTransfer transfer = new ProtostuffSocketTransfer(true);

        Protocol3 protocol3 = Protocol3.of((byte) 1, (short) 2, 3, 4, 5L, 6, "7", true, VO1.of(8));
        Collection<ByteBuf> byteBuf3s = transfer.encode(null, protocol3);
        for (ByteBuf byteBuf : byteBuf3s) {
            System.out.println(byteBuf.readableBytes());
            System.out.println(transfer.decode(null, byteBuf));
        }

        int[] a4 = new int[]{1, 2};
        Integer[] b4 = new Integer[]{3, 4};
        List<Integer> c4 = Arrays.asList(5, 6);
        Set<Integer> d4 = new HashSet<>();
        d4.add(7);
        d4.add(8);
        Map<Integer, Integer> e4 = new HashMap<>();
        e4.put(9, 10);
        e4.put(11, 12);

        Protocol4 protocol4 = Protocol4.of(a4, b4, c4, d4, e4);
        Collection<ByteBuf> byteBuf4s = transfer.encode(null, protocol4);
        for (ByteBuf byteBuf : byteBuf4s) {
            System.out.println(byteBuf.readableBytes());
            System.out.println(transfer.decode(null, byteBuf));
        }

        VO1[] a5 = new VO1[]{VO1.of(1), VO1.of(2), VO1.of(3)};
        List<VO1> b5 = new ArrayList<>();
        b5.add(VO1.of(4));
        b5.add(VO1.of(5));
        b5.add(VO1.of(6));
        b5.add(VO1.of(7));
        Map<VO1, Long> c5 = new HashMap<>();
        c5.put(VO1.of(8), 9L);
        c5.put(VO1.of(10), 11L);
        c5.put(VO1.of(12), 13L);
        Map<Integer, VO1> d5 = new HashMap<>();
        d5.put(14, VO1.of(15));
        d5.put(16, VO1.of(17));
        d5.put(18, VO1.of(19));
        List<VO2> e5 = new ArrayList<>();
        e5.add(VO2.of(Arrays.asList(VO1.of(20), VO1.of(21), VO1.of(22))));
        e5.add(VO2.of(Arrays.asList(VO1.of(23), VO1.of(24), VO1.of(25))));
        Map<Integer, VO2> f5 = new HashMap<>();
        f5.put(26, VO2.of(Arrays.asList(VO1.of(27), VO1.of(28), VO1.of(29))));
        f5.put(30, VO2.of(Arrays.asList(VO1.of(31), VO1.of(32), VO1.of(33))));

        Protocol5 protocol5 = Protocol5.of(a5, b5, c5, d5, e5, f5);
        Collection<ByteBuf> byteBuf5s = transfer.encode(null, protocol5);
        for (ByteBuf byteBuf : byteBuf5s) {
            System.out.println(byteBuf.readableBytes());
            System.out.println(transfer.decode(null, byteBuf));
        }
    }
}
