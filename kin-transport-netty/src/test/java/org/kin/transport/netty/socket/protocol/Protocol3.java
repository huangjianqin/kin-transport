package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2020/10/4
 */
@Protocol(id = 3)
public class Protocol3 extends SocketProtocol {
    private byte a;
    private short b;
    private int c;
    private float d;
    private long e;
    private double f;
    private String g;
    private boolean h;
    private VO1 vo1;

    public static Protocol3 of(byte a, short b, int c, float d, long e, double f, String g, boolean h, VO1 vo1) {
        Protocol3 inst = new Protocol3();
        inst.a = a;
        inst.b = b;
        inst.c = c;
        inst.d = d;
        inst.e = e;
        inst.f = f;
        inst.g = g;
        inst.h = h;
        inst.vo1 = vo1;
        return inst;
    }

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public short getB() {
        return b;
    }

    public void setB(short b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    public long getE() {
        return e;
    }

    public void setE(long e) {
        this.e = e;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public boolean isH() {
        return h;
    }

    public void setH(boolean h) {
        this.h = h;
    }

    public VO1 getVo1() {
        return vo1;
    }

    public void setVo1(VO1 vo1) {
        this.vo1 = vo1;
    }

    @Override
    public String toString() {
        return "Protocol3{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                ", d=" + d +
                ", e=" + e +
                ", f=" + f +
                ", g='" + g + '\'' +
                ", h=" + h +
                ", vo1=" + vo1 +
                "} " + super.toString();
    }
}

