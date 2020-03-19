package org.kin.transport.core;

import org.kin.transport.netty.core.protocol.ProtocolFactory;

/**
 * @author huangjianqin
 * @date 2019/7/4
 */
public class ProtocolFactoryTest {
    public static void main(String[] args) {
        ProtocolFactory.init("org.kin.transport");
        System.out.println(ProtocolFactory.createProtocol(1));
        System.out.println(ProtocolFactory.createProtocol(1, 1));
        System.out.println(ProtocolFactory.createProtocol(1, ""));

        System.out.println("----------------------------------------------------------");

        System.out.println(ProtocolFactory.createProtocol(2));
        System.out.println(ProtocolFactory.createProtocol(2, ""));
        System.out.println(ProtocolFactory.createProtocol(2, "", 2));
        System.out.println(ProtocolFactory.createProtocol(2, 1));
    }
}
