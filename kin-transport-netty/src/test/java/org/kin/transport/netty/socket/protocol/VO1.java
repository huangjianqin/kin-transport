package org.kin.transport.netty.socket.protocol;

/**
 * @author huangjianqin
 * @date 2020/10/4
 */
@ProtocolVO
public class VO1 {
    private int id;

    public static VO1 of(int id) {
        VO1 inst = new VO1();
        inst.id = id;
        return inst;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "VO1{" +
                "id=" + id +
                '}';
    }
}
