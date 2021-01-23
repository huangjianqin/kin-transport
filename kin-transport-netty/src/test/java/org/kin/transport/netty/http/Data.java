package org.kin.transport.netty.http;

import java.util.Map;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
public class Data {
    private int id;
    private Map<String, String> args;

    public Data(int id, Map<String, String> args) {
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

    public Map<String, String> getArgs() {
        return args;
    }

    public void setArgs(Map<String, String> args) {
        this.args = args;
    }
}
