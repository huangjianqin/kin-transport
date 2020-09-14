package org.kin.transport.netty.http.server;

import org.kin.framework.utils.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 与tomcat方式相同
 * 随机数(固定长度)+jvm标识
 * 因为随机数是固定长度的, 所以可以每个sessionid可以解析出jvm标识, 也就可以根据jvm标识指定请求发往某个server上
 *
 * @author huangjianqin
 * @date 2020/9/11
 */
public class DefaultSessionIdGenerator implements SessionIdGenerator {
    /** 随机数长度 */
    protected final int sessionIdLen;

    protected DefaultSessionIdGenerator(int sessionIdLen) {
        this.sessionIdLen = sessionIdLen;
    }

    @Override
    public String generate(String jvmRoute) {
        byte[] random = new byte[16];

        //随机数
        // Render the result as a String of hexadecimal digits
        // Start with enough space for sessionIdLen and medium route size
        StringBuilder buffer = new StringBuilder(2 * sessionIdLen + 20);

        int resultLenBytes = 0;

        while (resultLenBytes < sessionIdLen) {
            getRandomBytes(random);
            for (int j = 0;
                 j < random.length && resultLenBytes < sessionIdLen;
                 j++) {
                byte b1 = (byte) ((random[j] & 0xf0) >> 4);
                byte b2 = (byte) (random[j] & 0x0f);
                if (b1 < 10) {
                    buffer.append((char) ('0' + b1));
                } else {
                    buffer.append((char) ('A' + (b1 - 10)));
                }
                if (b2 < 10) {
                    buffer.append((char) ('0' + b2));
                } else {
                    buffer.append((char) ('A' + (b2 - 10)));
                }
                resultLenBytes++;
            }
        }

        //加上jvm唯一标识
        if (StringUtils.isNotBlank(jvmRoute)) {
            buffer.append('.').append(jvmRoute);
        }

        return buffer.toString();
    }


    private void getRandomBytes(byte[] bytes) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
