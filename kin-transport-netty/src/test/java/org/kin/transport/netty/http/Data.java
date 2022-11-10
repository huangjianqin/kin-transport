package org.kin.transport.netty.http;

import java.io.Serializable;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
public class Data implements Serializable {
    private static final long serialVersionUID = -8302541334080039680L;
    private int id;
    private Map<String, Object> args;

    public Data() {
    }

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
