package org.kin.transport.netty.http;

import java.util.Map;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
public class Data {
    private int id;
    private Map<String, Object> args;

    public Data(int id, Map<String, Object> args) {
        this.id = id;
        this.args = args;
    }

    //setter && getter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}
