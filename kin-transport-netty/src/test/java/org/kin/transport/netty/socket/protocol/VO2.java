package org.kin.transport.netty.socket.protocol;

import java.util.List;

/**
 * @author huangjianqin
 * @date 2020/10/5
 */
@ProtocolVO
public class VO2 {
    private List<VO1> a;

    public static VO2 of(List<VO1> a) {
        VO2 inst = new VO2();
        inst.a = a;
        return inst;
    }

    public List<VO1> getA() {
        return a;
    }

    public void setA(List<VO1> a) {
        this.a = a;
    }

    @Override
    public String toString() {
        return "VO2{" +
                "a=" + a +
                '}';
    }
}
