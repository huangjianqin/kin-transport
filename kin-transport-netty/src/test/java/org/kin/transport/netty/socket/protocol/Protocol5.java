package org.kin.transport.netty.socket.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2020/10/5
 */
@Protocol(id = 5)
public class Protocol5 extends SocketProtocol {
    private VO1[] a;
    private List<VO1> b;
    private Map<VO1, Long> c;
    private Map<Integer, VO1> d;
    private List<VO2> e;
    private Map<Integer, VO2> f;

    public static Protocol5 of(VO1[] a, List<VO1> b, Map<VO1, Long> c, Map<Integer, VO1> d, List<VO2> e, Map<Integer, VO2> f) {
        Protocol5 inst = new Protocol5();
        inst.a = a;
        inst.b = b;
        inst.c = c;
        inst.d = d;
        inst.e = e;
        inst.f = f;
        return inst;
    }


    public VO1[] getA() {
        return a;
    }

    public void setA(VO1[] a) {
        this.a = a;
    }

    public List<VO1> getB() {
        return b;
    }

    public void setB(List<VO1> b) {
        this.b = b;
    }

    public Map<VO1, Long> getC() {
        return c;
    }

    public void setC(Map<VO1, Long> c) {
        this.c = c;
    }

    public Map<Integer, VO1> getD() {
        return d;
    }

    public void setD(Map<Integer, VO1> d) {
        this.d = d;
    }

    public List<VO2> getE() {
        return e;
    }

    public void setE(List<VO2> e) {
        this.e = e;
    }

    public Map<Integer, VO2> getF() {
        return f;
    }

    public void setF(Map<Integer, VO2> f) {
        this.f = f;
    }

    @Override
    public String toString() {
        return "Protocol5{" +
                "a=" + Arrays.toString(a) +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", f=" + f +
                "} " + super.toString();
    }
}

