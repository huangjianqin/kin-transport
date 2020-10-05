package org.kin.transport.netty.socket.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author huangjianqin
 * @date 2020/10/5
 */
@Protocol(id = 4)
public class Protocol4 extends SocketProtocol {
    private int[] a;
    private Integer[] b;
    private List<Integer> c;
    private Set<Integer> d;
    private Map<Integer, Integer> e;

    public static Protocol4 of(int[] a, Integer[] b, List<Integer> c, Set<Integer> d, Map<Integer, Integer> e) {
        Protocol4 inst = new Protocol4();
        inst.a = a;
        inst.b = b;
        inst.c = c;
        inst.d = d;
        inst.e = e;
        return inst;
    }

    public int[] getA() {
        return a;
    }

    public void setA(int[] a) {
        this.a = a;
    }

    public Integer[] getB() {
        return b;
    }

    public void setB(Integer[] b) {
        this.b = b;
    }

    public List<Integer> getC() {
        return c;
    }

    public void setC(List<Integer> c) {
        this.c = c;
    }

    public Set<Integer> getD() {
        return d;
    }

    public void setD(Set<Integer> d) {
        this.d = d;
    }

    public Map<Integer, Integer> getE() {
        return e;
    }

    public void setE(Map<Integer, Integer> e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "Protocol4{" +
                "a=" + Arrays.toString(a) +
                ", b=" + Arrays.toString(b) +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                "} " + super.toString();
    }
}
