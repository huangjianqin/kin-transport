package org.kin.transport.netty.http;

import org.kin.transport.netty.AbstractTransportOption;

/**
 * @author huangjianqin
 * @date 2020/8/27
 */
public abstract class AbstractHttpTransportOption<IN, MSG, OUT, O extends AbstractHttpTransportOption<IN, MSG, OUT, O>>
        extends AbstractTransportOption<IN, MSG, OUT, O> {
}
