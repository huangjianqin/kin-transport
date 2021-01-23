package org.kin.transport.netty.http.server;

import org.kin.framework.proxy.ProxyInvoker;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.JSON;
import org.kin.framework.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2021/1/23
 */
class MappingAnnoServlet extends AbstractServlet {
    /** 目标方法的调用代理类 */
    private final ProxyInvoker<Object> invoker;
    private final List<RequestParamAnnoConfig> paramConfigs;

    public MappingAnnoServlet(ProxyInvoker<Object> invoker, Method method) {
        this.invoker = invoker;
        this.paramConfigs = parse(method);
    }

    /**
     * 解析mapping注解方法参数配置
     */
    private List<RequestParamAnnoConfig> parse(Method method) {
        Parameter[] parameters = method.getParameters();
        List<RequestParamAnnoConfig> paramConfigs = new ArrayList<>(parameters.length);

        for (Parameter parameter : parameters) {
            String attrName = "";
            boolean require = false;
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (Objects.nonNull(requestParam)) {
                attrName = requestParam.value();
                require = requestParam.require();
            }
            paramConfigs.add(new RequestParamAnnoConfig(attrName, parameter.getType(), require));
        }

        return Collections.unmodifiableList(paramConfigs);
    }

    /**
     * 根据参数配置解析参数值
     */
    protected Object[] parseParams(ServletRequest request, ServletResponse response) {
        List<Object> params = new ArrayList<>(paramConfigs.size());

        for (RequestParamAnnoConfig paramConfig : paramConfigs) {
            Class<?> type = paramConfig.type;
            if (ServletRequest.class.equals(type)) {
                params.add(request);
            } else if (ServletResponse.class.equals(type)) {
                params.add(response);
            } else {
                String attrName = paramConfig.attrName;
                if (StringUtils.isNotBlank(attrName)) {
                    Object attrValue = request.getParams().get(attrName);
                    if (Objects.nonNull(attrValue)) {
                        if (attrValue instanceof Map) {
                            //结构化数据, 需先转成json, 再反序列化
                            params.add(JSON.read(JSON.write(attrValue), type));
                        } else {
                            params.add(JSON.read(attrValue.toString(), type));
                        }
                    } else {
                        //找不到属性值
                        if (paramConfig.require) {
                            throw new IllegalArgumentException(String.format("http request doesn't have attribute with name '%s'", attrName));
                        }
                        params.add(ClassUtils.getDefaultValue(type));
                    }
                } else {
                    params.add(ClassUtils.getDefaultValue(type));
                }
            }
        }

        return params.toArray(new Object[0]);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * mapping注解方法参数配置
     */
    private class RequestParamAnnoConfig {
        /** 参数字段名 */
        private final String attrName;
        /** 参数类型 */
        private final Class<?> type;
        /** 是否必须存在, 如果检查到不存在, 则报错 */
        private final boolean require;

        public RequestParamAnnoConfig(String attrName, Class<?> type, boolean require) {
            this.attrName = attrName;
            this.type = type;
            this.require = require;
        }
    }
}
